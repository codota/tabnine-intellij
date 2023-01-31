package com.tabnine.inline;

import static com.tabnine.general.Utils.*;
import static com.tabnine.prediction.CompletionFacade.getFilename;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.ObjectUtils;
import com.tabnine.balloon.FirstSuggestionHintTooltip;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.notifications.shown.SnippetShownRequest;
import com.tabnine.binary.requests.notifications.shown.SuggestionDroppedReason;
import com.tabnine.binary.requests.notifications.shown.SuggestionShownRequest;
import com.tabnine.capabilities.CapabilitiesService;
import com.tabnine.capabilities.Capability;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.capabilities.SuggestionsModeService;
import com.tabnine.general.CompletionKind;
import com.tabnine.general.CompletionsEventSender;
import com.tabnine.general.DependencyContainer;
import com.tabnine.general.SuggestionTrigger;
import com.tabnine.inline.render.GraphicsUtilsKt;
import com.tabnine.intellij.completions.CompletionUtils;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InlineCompletionHandler {
  private final CompletionFacade completionFacade;
  private final BinaryRequestFacade binaryRequestFacade;
  private final SuggestionsModeService suggestionsModeService;
  private final CompletionsEventSender completionsEventSender =
      DependencyContainer.instanceOfCompletionsEventSender();
  private Future<?> lastDebounceRenderTask = null;
  private Future<?> lastFetchAndRenderTask = null;
  private Future<?> lastFetchInBackgroundTask = null;

  public InlineCompletionHandler(
      CompletionFacade completionFacade,
      BinaryRequestFacade binaryRequestFacade,
      SuggestionsModeService suggestionsModeService) {
    this.completionFacade = completionFacade;
    this.binaryRequestFacade = binaryRequestFacade;
    this.suggestionsModeService = suggestionsModeService;
  }

  public void retrieveAndShowCompletion(
      @NotNull Editor editor,
      int offset,
      @Nullable TabNineCompletion lastShownSuggestion,
      @NotNull String userInput,
      @NotNull CompletionAdjustment completionAdjustment) {
    Integer tabSize = GraphicsUtilsKt.getTabSize(editor);

    ObjectUtils.doIfNotNull(lastFetchInBackgroundTask, task -> task.cancel(false));
    ObjectUtils.doIfNotNull(lastFetchAndRenderTask, task -> task.cancel(false));
    ObjectUtils.doIfNotNull(lastDebounceRenderTask, task -> task.cancel(false));

    List<TabNineCompletion> cachedCompletions =
        InlineCompletionCache.getInstance().retrieveAdjustedCompletions(editor, userInput);
    if (!cachedCompletions.isEmpty()) {
      renderCachedCompletions(editor, offset, tabSize, cachedCompletions, completionAdjustment);
      return;
    }

    if (lastShownSuggestion != null) {
      // this means that the user did not type as suggested -
      // we couldn't find completions in the cache, but there was a suggestion rendered
      SuggestionDroppedReason reason =
          completionAdjustment instanceof LookAheadCompletionAdjustment
              ? SuggestionDroppedReason.ScrollLookAhead
              : SuggestionDroppedReason.UserNotTypedAsSuggested;
      sendSuggestionDroppedEvent(
          editor, lastShownSuggestion, reason);
    }

    ApplicationManager.getApplication()
        .invokeLater(
            () ->
                renderNewCompletions(
                    editor,
                    tabSize,
                    getCurrentEditorOffset(editor, userInput),
                    editor.getDocument().getModificationStamp(),
                    completionAdjustment));
  }

  public void sendSuggestionDroppedEvent(
      @NotNull Editor editor,
      @NotNull TabNineCompletion suggestion,
      SuggestionDroppedReason reason) {
    try {
      String filename =
          getFilename(FileDocumentManager.getInstance().getFile(editor.getDocument()));
      if (filename == null) {
        Logger.getInstance(getClass())
            .warn("Failed to send suggestion dropped event - filename is null");
        return;
      }

      completionsEventSender.sendSuggestionDropped(
          suggestion.getNetLength(), filename, reason, suggestion.completionMetadata);
    } catch (Throwable e) {
      Logger.getInstance(getClass()).warn("Failed to send suggestion dropped event", e);
    }
  }

  private void renderCachedCompletions(
      @NotNull Editor editor,
      int offset,
      Integer tabSize,
      @NotNull List<TabNineCompletion> cachedCompletions,
      @NotNull CompletionAdjustment completionAdjustment) {
    showInlineCompletion(editor, cachedCompletions, offset, null);
    lastFetchInBackgroundTask =
        executeThread(
            () -> retrieveInlineCompletion(editor, offset, tabSize, completionAdjustment));
  }

  private int getCurrentEditorOffset(@NotNull Editor editor, @NotNull String userInput) {
    return editor.getCaretModel().getOffset()
        + (ApplicationManager.getApplication().isUnitTestMode() ? userInput.length() : 0);
  }

  private void renderNewCompletions(
      @NotNull Editor editor,
      Integer tabSize,
      int offset,
      long modificationStamp,
      @NotNull CompletionAdjustment completionAdjustment) {
    lastFetchAndRenderTask =
        executeThread(
            () -> {
              CompletionTracker.updateLastCompletionRequestTime(editor);
              List<TabNineCompletion> beforeDebounceCompletions =
                  retrieveInlineCompletion(editor, offset, tabSize, completionAdjustment);
              long debounceTime = CompletionTracker.calcDebounceTime(editor, completionAdjustment);

              if (debounceTime == 0) {
                rerenderCompletion(
                    editor,
                    beforeDebounceCompletions,
                    offset,
                    modificationStamp,
                    completionAdjustment);
                return;
              }

              refetchCompletionsAfterDebounce(
                  editor, tabSize, offset, modificationStamp, completionAdjustment, debounceTime);
            });
  }

  private void refetchCompletionsAfterDebounce(
      @NotNull Editor editor,
      Integer tabSize,
      int offset,
      long modificationStamp,
      @NotNull CompletionAdjustment completionAdjustment,
      long debounceTime) {
    lastDebounceRenderTask =
        executeThread(
            () -> {
              List<TabNineCompletion> completions =
                  retrieveInlineCompletion(editor, offset, tabSize, completionAdjustment);
              rerenderCompletion(
                  editor, completions, offset, modificationStamp, completionAdjustment);
            },
            debounceTime,
            TimeUnit.MILLISECONDS);
  }

  private void rerenderCompletion(
      @NotNull Editor editor,
      List<TabNineCompletion> completions,
      int offset,
      long modificationStamp,
      @NotNull CompletionAdjustment completionAdjustment) {
    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              if (shouldCancelRendering(editor, modificationStamp, offset)) {
                return;
              }
              if (shouldRemovePopupCompletions(completionAdjustment)) {
                completions.removeIf(completion -> !completion.isSnippet());
              }
              showInlineCompletion(
                  editor,
                  completions,
                  offset,
                  (completion) -> afterCompletionShown(completion, editor));
            });
  }

  private boolean shouldCancelRendering(
      @NotNull Editor editor, long modificationStamp, int offset) {
    if (isUnitTestMode()) {
      return false;
    }
    boolean isModificationStampChanged =
        modificationStamp != editor.getDocument().getModificationStamp();
    boolean isOffsetChanged = offset != editor.getCaretModel().getOffset();
    return isModificationStampChanged || isOffsetChanged;
  }

  /**
   * remove popup completions when 1. the suggestion mode is HYBRID and 2. the completion adjustment
   * type is not LookAhead
   */
  private boolean shouldRemovePopupCompletions(@NotNull CompletionAdjustment completionAdjustment) {
    return suggestionsModeService.getSuggestionMode() == SuggestionsMode.HYBRID
        && completionAdjustment.getSuggestionTrigger() != SuggestionTrigger.LookAhead;
  }

  private List<TabNineCompletion> retrieveInlineCompletion(
      @NotNull Editor editor,
      int offset,
      Integer tabSize,
      @NotNull CompletionAdjustment completionAdjustment) {
    AutocompleteResponse completionsResponse =
        this.completionFacade.retrieveCompletions(editor, offset, tabSize, completionAdjustment);

    if (completionsResponse == null || completionsResponse.results.length == 0) {
      return Collections.emptyList();
    }

    return createCompletions(
        completionsResponse,
        editor.getDocument(),
        offset,
        completionAdjustment.getSuggestionTrigger());
  }

  private void showInlineCompletion(
      @NotNull Editor editor,
      List<TabNineCompletion> completions,
      int offset,
      @Nullable OnCompletionPreviewUpdatedCallback onCompletionPreviewUpdatedCallback) {
    if (completions.isEmpty()) {
      return;
    }
    InlineCompletionCache.getInstance().store(editor, completions);

    TabNineCompletion displayedCompletion =
        CompletionPreview.createInstance(editor, completions, offset);

    if (displayedCompletion == null) {
      return;
    }

    if (onCompletionPreviewUpdatedCallback != null) {
      onCompletionPreviewUpdatedCallback.onCompletionPreviewUpdated(displayedCompletion);
    }
  }

  private void afterCompletionShown(TabNineCompletion completion, Editor editor) {
    if (CapabilitiesService.getInstance()
        .isCapabilityEnabled(Capability.FIRST_SUGGESTION_HINT_ENABLED)) {
      FirstSuggestionHintTooltip.handle(editor);
    }

    if (completion.completionMetadata == null) return;
    Boolean isCached = completion.completionMetadata.is_cached();
    // binary is not supporting api version ^4.0.57
    if (isCached == null) return;

    try {
      String filename =
          getFilename(FileDocumentManager.getInstance().getFile(editor.getDocument()));
      if (filename == null) {
        Logger.getInstance(getClass())
            .warn("Could not send SuggestionShown request. the filename is null");
        return;
      }
      this.binaryRequestFacade.executeRequest(
          new SuggestionShownRequest(
              completion.getNetLength(), filename, completion.completionMetadata));

      if (completion.completionMetadata.getCompletion_kind() == CompletionKind.Snippet
          && !isCached) {
        Map<String, Object> context = completion.completionMetadata.getSnippet_context();
        if (context == null) {
          Logger.getInstance(getClass())
              .warn("Could not send SnippetShown request. intent is null");
          return;
        }

        this.binaryRequestFacade.executeRequest(new SnippetShownRequest(filename, context));
      }
    } catch (RuntimeException e) {
      // swallow - nothing to do with this
    }
  }

  private List<TabNineCompletion> createCompletions(
      AutocompleteResponse completions,
      @NotNull Document document,
      int offset,
      SuggestionTrigger suggestionTrigger) {
    return IntStream.range(0, completions.results.length)
        .mapToObj(
            index ->
                CompletionUtils.createTabnineCompletion(
                    document,
                    offset,
                    completions.old_prefix,
                    completions.results[index],
                    index,
                    suggestionTrigger))
        .filter(completion -> completion != null && !completion.getSuffix().isEmpty())
        .collect(Collectors.toList());
  }
}
