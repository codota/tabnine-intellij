package com.tabnine.inline;

import static com.tabnine.prediction.CompletionFacade.getFilename;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.autocomplete.UserIntent;
import com.tabnine.binary.requests.notifications.shown.SnippetShownRequest;
import com.tabnine.general.CompletionKind;
import com.tabnine.intellij.completions.CompletionUtils;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InlineCompletionHandler {
  private final CompletionFacade completionFacade;
  private final BinaryRequestFacade binaryRequestFacade;
  private Future<?> lastPreviewTask = null;

  public InlineCompletionHandler(
      CompletionFacade completionFacade, BinaryRequestFacade binaryRequestFacade) {
    this.completionFacade = completionFacade;
    this.binaryRequestFacade = binaryRequestFacade;
  }

  public void retrieveAndShowCompletion(@NotNull Editor editor) {
    int offset = editor.getCaretModel().getOffset();
    long modificationStamp = editor.getDocument().getModificationStamp();

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      List<TabNineCompletion> completions = retrieveInlineCompletion(editor, offset);
      rerenderCompletion(editor, completions, offset, modificationStamp);
    } else {
      ObjectUtils.doIfNotNull(lastPreviewTask, task -> task.cancel(false));

      lastPreviewTask =
          AppExecutorUtil.getAppExecutorService()
              .submit(
                  () -> {
                    List<TabNineCompletion> completions = retrieveInlineCompletion(editor, offset);
                    rerenderCompletion(editor, completions, offset, modificationStamp);
                  });
    }
  }

  private void rerenderCompletion(
      @NotNull Editor editor,
      List<TabNineCompletion> completions,
      int offset,
      long modificationStamp) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      showInlineCompletion(editor, completions, offset, null);
      return;
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

  private List<TabNineCompletion> retrieveInlineCompletion(@NotNull Editor editor, int offset) {
    AutocompleteResponse completionsResponse =
        this.completionFacade.retrieveCompletions(editor, offset);

    if (completionsResponse == null || completionsResponse.results.length == 0) {
      return Collections.emptyList();
    }

    return createCompletions(completionsResponse, editor.getDocument(), offset);
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
        UserIntent intent = completion.snippet_intent;
        boolean intentIsNull = intent == null;
        boolean filenameIsNull = filename == null;
        if (filenameIsNull || intentIsNull) {
          logSnippetShownWarn(intentIsNull, filenameIsNull);
          return;
        }

        this.binaryRequestFacade.executeRequest(new SnippetShownRequest(filename, intent));
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
                    completions.snippet_intent))
        .filter(completion -> !completion.getSuffix().isEmpty())
        .collect(Collectors.toList());
  }
}
