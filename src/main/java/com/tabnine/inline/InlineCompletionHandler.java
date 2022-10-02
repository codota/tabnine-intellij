package com.tabnine.inline;

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

  public InlineCompletionHandler(
      CompletionFacade completionFacade,
      BinaryRequestFacade binaryRequestFacade,
      SuggestionsModeService suggestionsModeService) {
    this.completionFacade = completionFacade;
    this.binaryRequestFacade = binaryRequestFacade;
    this.suggestionsModeService = suggestionsModeService;
  }

  public void retrieveAndShowCompletion(
      @NotNull Editor editor, int offset, @NotNull CompletionAdjustment completionAdjustment) {
    long modificationStamp = editor.getDocument().getModificationStamp();
    Integer tabSize = GraphicsUtilsKt.getTabSize(editor);

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      List<TabNineCompletion> completions =
          retrieveInlineCompletion(editor, offset, tabSize, completionAdjustment);
      rerenderCompletion(editor, completions, offset, modificationStamp, completionAdjustment);
      if (!completions.isEmpty()) {
        FirstSuggestionHintTooltip.handle(editor);
      }
      return;
    }

    ObjectUtils.doIfNotNull(lastFetchAndRenderTask, task -> task.cancel(false));
    ObjectUtils.doIfNotNull(lastRenderTask, task -> task.cancel(false));

    lastFetchAndRenderTask =
        AppExecutorUtil.getAppExecutorService()
            .submit(
                () -> {
                  CompletionTracker.updateLastCompletionRequestTime(editor);
                  List<TabNineCompletion> completions =
                      retrieveInlineCompletion(editor, offset, tabSize, completionAdjustment);
                  lastRenderTask =
                      scheduler.schedule(
                          () ->
                              rerenderCompletion(
                                  editor,
                                  completions,
                                  offset,
                                  modificationStamp,
                                  completionAdjustment),
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
    if (shouldRemovePopupCompletions(completionAdjustment)) {
      completions.removeIf(completion -> !completion.isSnippet());
    }
    ApplicationManager.getApplication()
        .invokeLater(
            () ->
                showInlineCompletion(
                    editor,
                    completions,
                    offset,
                    (completion) -> afterCompletionShown(completion, editor.getDocument())),
            unused -> modificationStamp != editor.getDocument().getModificationStamp());
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

    TabNineCompletion displayedCompletion =
        CompletionPreview.createInstance(editor, completions, offset);

    if (displayedCompletion == null) {
      return;
    }

    if (onCompletionPreviewUpdatedCallback != null) {
      onCompletionPreviewUpdatedCallback.onCompletionPreviewUpdated(displayedCompletion);
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
