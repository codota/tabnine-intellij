package com.tabnine.inline;

import static com.tabnine.general.Utils.executeThread;
import static com.tabnine.general.Utils.executeUIThreadWithDelay;
import static com.tabnine.prediction.CompletionFacade.getFilename;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.balloon.FirstSuggestionHintTooltip;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.autocomplete.SnippetContext;
import com.tabnine.binary.requests.notifications.shown.SnippetShownRequest;
import com.tabnine.binary.requests.notifications.shown.SuggestionShownRequest;
import com.tabnine.capabilities.CapabilitiesService;
import com.tabnine.capabilities.Capability;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.capabilities.SuggestionsModeService;
import com.tabnine.general.CompletionKind;
import com.tabnine.general.SuggestionTrigger;
import com.tabnine.inline.render.GraphicsUtilsKt;
import com.tabnine.intellij.completions.CompletionUtils;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InlineCompletionHandler {
  private static final ScheduledExecutorService scheduler =
      AppExecutorUtil.getAppScheduledExecutorService();
  private final CompletionFacade completionFacade;
  private final BinaryRequestFacade binaryRequestFacade;
  private final SuggestionsModeService suggestionsModeService;
  private Future<?> lastRenderTask = null;
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
      String userInput,
      @NotNull CompletionAdjustment completionAdjustment) {
    long modificationStamp = editor.getDocument().getModificationStamp();
    Integer tabSize = GraphicsUtilsKt.getTabSize(editor);

    ObjectUtils.doIfNotNull(lastFetchInBackgroundTask, task -> task.cancel(false));
    ObjectUtils.doIfNotNull(lastFetchAndRenderTask, task -> task.cancel(false));
    ObjectUtils.doIfNotNull(lastRenderTask, task -> task.cancel(false));

    List<TabNineCompletion> cachedCompletions =
        InlineCompletionCache.getInstance().retrieveAdjustedCompletions(editor, userInput);
    if (!cachedCompletions.isEmpty()) {
      showInlineCompletion(editor, cachedCompletions, offset, null);
      lastFetchInBackgroundTask =
          executeThread(
              () -> retrieveInlineCompletion(editor, offset, tabSize, completionAdjustment));
      return;
    }

    lastFetchAndRenderTask =
        executeThread(
            () -> {
              CompletionTracker.updateLastCompletionRequestTime(editor);
              List<TabNineCompletion> completions =
                  retrieveInlineCompletion(editor, offset, tabSize, completionAdjustment);
              lastRenderTask =
                  executeUIThreadWithDelay(
                      () ->
                          rerenderCompletion(
                              editor, completions, offset, modificationStamp, completionAdjustment),
                      CompletionTracker.calcDebounceTime(editor, completionAdjustment),
                      TimeUnit.MILLISECONDS);
            });
  }

  private void rerenderCompletion(
      @NotNull Editor editor,
      List<TabNineCompletion> completions,
      int offset,
      long modificationStamp,
      @NotNull CompletionAdjustment completionAdjustment) {
    if (shouldCancelRendering(editor, modificationStamp, offset)) {
      return;
    }
    if (shouldRemovePopupCompletions(completionAdjustment)) {
      completions.removeIf(completion -> !completion.isSnippet());
    }
    showInlineCompletion(
        editor, completions, offset, (completion) -> afterCompletionShown(completion, editor));
  }

  private boolean shouldCancelRendering(
      @NotNull Editor editor, long modificationStamp, int offset) {
    boolean isModificationStampChanged =
        modificationStamp != editor.getDocument().getModificationStamp();
    int editorOffset =
        editor.getCaretModel().getOffset()
            + (ApplicationManager.getApplication().isUnitTestMode() ? 1 : 0);
    boolean isOffsetChanged = offset != editorOffset;
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

    // binary is not supporting api version ^4.0.57
    if (completion.isCached == null) return;

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
              completion.origin, completion.completionKind, completion.getNetLength(), filename));

      if (completion.completionKind == CompletionKind.Snippet && !completion.isCached) {
        SnippetContext context = completion.snippet_context;
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
                    completions.snippet_context,
                    suggestionTrigger))
        .filter(completion -> !completion.getSuffix().isEmpty())
        .collect(Collectors.toList());
  }
}
