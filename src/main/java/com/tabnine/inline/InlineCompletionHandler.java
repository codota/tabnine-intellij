package com.tabnine.inline;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Key;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.balloon.FirstSuggestionHintTooltip;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.autocomplete.SnippetContext;
import com.tabnine.binary.requests.notifications.shown.SnippetShownRequest;
import com.tabnine.capabilities.CapabilitiesService;
import com.tabnine.capabilities.Capability;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.capabilities.SuggestionsModeService;
import com.tabnine.general.CompletionKind;
import com.tabnine.inline.render.GraphicsUtilsKt;
import com.tabnine.intellij.completions.CompletionUtils;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tabnine.general.DependencyContainer.instanceOfTabNineCompletionsCache;
import static com.tabnine.prediction.CompletionFacade.getFilename;

public class InlineCompletionHandler {
  private static final ScheduledExecutorService scheduler =
      AppExecutorUtil.getAppScheduledExecutorService();
  private final CompletionFacade completionFacade;
  private final BinaryRequestFacade binaryRequestFacade;
  private final SuggestionsModeService suggestionsModeService;
  private final CompletionCache completionCache = instanceOfTabNineCompletionsCache();

  private Future<?> lastPreviewTask = null;

  private static final Key<List<TabNineCompletion>> TABNINE_COMPLETION_CACHE =
      Key.create("TABNINE_COMPLETION_CACHE");

  public InlineCompletionHandler(
      CompletionFacade completionFacade,
      BinaryRequestFacade binaryRequestFacade,
      SuggestionsModeService suggestionsModeService) {
    this.completionFacade = completionFacade;
    this.binaryRequestFacade = binaryRequestFacade;
    this.suggestionsModeService = suggestionsModeService;
  }

  public void retrieveAndShowCompletion(@NotNull Editor editor, int offset) {
    retrieveAndShowCompletion(editor, offset, null);
  }

  public void retrieveAndShowCompletion(
      @NotNull Editor editor, int offset, @Nullable CompletionAdjustment completionAdjustment) {
    long modificationStamp = editor.getDocument().getModificationStamp();
    Integer tabSize = GraphicsUtilsKt.getTabSize(editor);

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      retrieveInlineCompletion(
          editor,
          offset,
          tabSize,
          completionAdjustment,
          completions -> {
            rerenderCompletion(
                editor, completions, offset, modificationStamp, completionAdjustment);
            if (!completions.isEmpty()) {
              FirstSuggestionHintTooltip.handle(editor);
            }
          });
      return;
    }

    retrieveInlineCompletion(
        editor,
        offset,
        tabSize,
        completionAdjustment,
        completions -> {
          rerenderCompletion(editor, completions, offset, modificationStamp, completionAdjustment);
        });
  }

  private void rerenderCompletion(
      @NotNull Editor editor,
      List<TabNineCompletion> completions,
      int offset,
      long modificationStamp,
      @Nullable CompletionAdjustment completionAdjustment) {
    if (shouldRemovePopupCompletions(completionAdjustment)) {
      completions.removeIf(completion -> !completion.isSnippet());
    }
    showInlineCompletion(
        editor,
        completions,
        offset,
        (completion) -> afterCompletionShown(completion, editor.getDocument()));
  }

  /**
   * remove popup completions when 1. the suggestion mode is HYBRID and 2. the completion adjustment
   * type is not LookAhead
   */
  private boolean shouldRemovePopupCompletions(
      @Nullable CompletionAdjustment completionAdjustment) {
    return suggestionsModeService.getSuggestionMode() == SuggestionsMode.HYBRID
        && (completionAdjustment == null
        || completionAdjustment.getType() != CompletionAdjustmentType.LookAhead);
  }

  private void retrieveInlineCompletion(
      @NotNull Editor editor,
      int offset,
      Integer tabSize,
      @Nullable CompletionAdjustment completionAdjustment,
      OnCompletionFetchedCallback onCompletionFetchedCallback) {

    AutocompleteResponse completionsResponse =
        this.completionFacade.retrieveCompletions(
            editor, offset, tabSize, completionAdjustment);

    List<TabNineCompletion> completions =
        completionsResponse == null || completionsResponse.results.length == 0
            ? Collections.emptyList()
            : createCompletions(completionsResponse, editor.getDocument(), offset);

    onCompletionFetchedCallback.onCompletionFetched(completions);
  }

  private void showInlineCompletion(
      @NotNull Editor editor,
      List<TabNineCompletion> completions,
      int offset,
      @Nullable OnCompletionPreviewUpdatedCallback onCompletionPreviewUpdatedCallback) {
    if (completions.isEmpty()) {
      return;
    }

    TabNineCompletion displayedCompletion =
        CompletionPreview.createInstance(editor, completions, offset);

    if (displayedCompletion == null) {
      return;
    }

    if (onCompletionPreviewUpdatedCallback != null) {
      onCompletionPreviewUpdatedCallback.onCompletionPreviewUpdated(displayedCompletion);
    }

    if (CapabilitiesService.getInstance()
        .isCapabilityEnabled(Capability.FIRST_SUGGESTION_HINT_ENABLED)
        && !completions.isEmpty()) {
      FirstSuggestionHintTooltip.handle(editor);
    }
  }

  private void afterCompletionShown(TabNineCompletion completion, Document document) {
    // binary is not supporting api version ^4.0.57
    if (completion.isCached == null) return;

    if (completion.completionKind == CompletionKind.Snippet && !completion.isCached) {
      try {
        String filename = getFilename(FileDocumentManager.getInstance().getFile(document));
        SnippetContext context = completion.snippet_context;
        boolean contextIsNull = context == null;
        boolean filenameIsNull = filename == null;
        if (filenameIsNull || contextIsNull) {
          logSnippetShownWarn(contextIsNull, filenameIsNull);
          return;
        }

        this.binaryRequestFacade.executeRequest(new SnippetShownRequest(filename, context));
      } catch (RuntimeException e) {
        // swallow - nothing to do with this
      }
    }
  }

  private void logSnippetShownWarn(boolean intentIsNull, boolean filenameIsNull) {
    Logger.getInstance(getClass())
        .warn(
            String.format(
                "Could not send SnippetShown request. intent is null: %s, filename is null: %s",
                intentIsNull, filenameIsNull));
  }

  private List<TabNineCompletion> createCompletions(
      AutocompleteResponse completions, @NotNull Document document, int offset) {
    return IntStream.range(0, completions.results.length)
        .mapToObj(
            index ->
                CompletionUtils.createTabnineCompletion(
                    document,
                    offset,
                    completions.old_prefix,
                    completions.results[index],
                    index,
                    completions.snippet_context))
        .filter(completion -> !completion.getSuffix().isEmpty())
        .collect(Collectors.toList());
  }
}
