package com.tabnine.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.DocumentUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBus;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.autocomplete.UserIntent;
import com.tabnine.binary.requests.notifications.shown.SnippetShownRequest;
import com.tabnine.general.CompletionKind;
import com.tabnine.general.DependencyContainer;
import com.tabnine.intellij.completions.CompletionUtils;
import com.tabnine.intellij.completions.LimitedSecletionsChangedNotifier;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static com.tabnine.prediction.CompletionFacade.getFilename;

public class InlineCompletionHandler implements CodeInsightActionHandler {
  private static final Set<Character> CLOSING_CHARACTERS =
      ContainerUtil.set('\'', '"', '`', ']', '}', ')', '>');
  public static final int DEBOUNCE_MILLIS = 350;

  private final CompletionFacade completionFacade =
      DependencyContainer.instanceOfCompletionFacade();
  private final BinaryRequestFacade binaryRequestFacade =
      DependencyContainer.instanceOfBinaryRequestFacade();
  private final MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
  private Future<?> lastPreviewTask = null;
  private final boolean myForward;

  private static boolean isLocked;

  InlineCompletionHandler(boolean forward) {
    this.myForward = forward;
  }

  void invoke(@NotNull Editor editor, @NotNull PsiFile file, int offset) {
    Document document = editor.getDocument();

    // if we cannot modify this file, return
    if (editor.isViewer() || document.getRangeGuard(offset, offset) != null) {
      document.fireReadOnlyModificationAttempt();
      EditorModificationUtil.checkModificationAllowed(editor);
      return;
    }

    if (isInTheMiddleOfWord(document, offset)) {
      return;
    }

    // data about previous completions, or just a new fresh state for the editor
    final CompletionState completionState = CompletionState.findOrCreateCompletionState(editor);
    int lastDisplayedCompletionIndex = completionState.lastDisplayedCompletionIndex;

    boolean noOldSuggestion = lastDisplayedCompletionIndex == -1;
    boolean editorLocationHasChanged = completionState.lastStartOffset != offset;
    boolean documentChanged =
        completionState.lastModificationStamp != document.getModificationStamp();

    if (noOldSuggestion || editorLocationHasChanged || documentChanged) {
      // start a new query
      completionState.lastDisplayedCompletionIndex = -1;
      retrieveAndShowInlineCompletion(editor, file, completionState, offset);
    } else {
      showInlineCompletion(editor, file, completionState, completionState.lastStartOffset);
    }
  }

  private boolean isInTheMiddleOfWord(@NotNull Document document, int offset) {
    try {
      if (DocumentUtil.isAtLineEnd(offset, document)) {
        return false;
      }

      char nextChar = document.getText(new TextRange(offset, offset + 1)).charAt(0);
      return !CLOSING_CHARACTERS.contains(nextChar) && !Character.isWhitespace(nextChar);
    } catch (Throwable e) {
      Logger.getInstance(getClass())
          .debug("Could not determine if text is in the middle of word, skipping: ", e);
    }

    return false;
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    int caretOffset = editor.getCaretModel().getOffset();
    invoke(editor, file, caretOffset);
  }

  private void showInlineCompletion(
      @NotNull Editor editor,
      @NotNull PsiFile file,
      CompletionState completionState,
      int startOffset) {
    showInlineCompletion(editor, file, completionState, startOffset, null);
  }

  private void showInlineCompletion(
      @NotNull Editor editor,
      @NotNull PsiFile file,
      CompletionState completionState,
      int startOffset,
      @Nullable OnCompletionPreviewUpdatedCallback onCompletionPreviewUpdatedCallback) {
    if (completionState.suggestions == null || completionState.suggestions.isEmpty()) {
      return;
    }
    int diff = myForward ? 1 : -1;
    int size = completionState.suggestions.size();
    // Make sure to keep the index in the valid range
    int nextIndex = (completionState.lastDisplayedCompletionIndex + diff + size) % size;
    final TabNineCompletion nextSuggestion = completionState.suggestions.get(nextIndex);
    if (nextSuggestion == null) {
      return;
    }

    CompletionPreview preview = CompletionPreview.getInstance(editor, file);
    completionState.lastDisplayedPreview =
        preview.updatePreview(completionState.suggestions, nextIndex, startOffset);
    if (onCompletionPreviewUpdatedCallback != null) {
      onCompletionPreviewUpdatedCallback.onCompletionPreviewUpdated(
          completionState.suggestions.get(nextIndex));
    }
    completionState.lastDisplayedCompletionIndex = nextIndex;
    completionState.lastStartOffset = startOffset;
    completionState.lastModificationStamp = editor.getDocument().getModificationStamp();
  }

  private void retrieveInlineCompletion(
      @NotNull Editor editor, CompletionState completionState, int startOffset) {
    AutocompleteResponse completionsResponse =
        this.completionFacade.retrieveCompletions(editor, startOffset);

    if (completionsResponse == null || completionsResponse.results.length == 0) {
      return;
    }
    if (isLocked != completionsResponse.is_locked) {
      isLocked = completionsResponse.is_locked;
      this.messageBus
          .syncPublisher(LimitedSecletionsChangedNotifier.LIMITED_SELECTIONS_CHANGED_TOPIC)
          .limitedChanged(completionsResponse.is_locked);
    }
    completionState.suggestions =
        createCompletions(completionsResponse, editor.getDocument(), startOffset);
  }

  private void retrieveAndShowInlineCompletion(
      @NotNull Editor editor,
      @NotNull PsiFile file,
      CompletionState completionState,
      int startOffset) {
    final Document document = editor.getDocument();
    final long lastModified = document.getModificationStamp();

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      retrieveInlineCompletion(editor, completionState, startOffset);
      completionState.resetStats();
      showInlineCompletion(editor, file, completionState, startOffset);
    } else {
      ObjectUtils.doIfNotNull(lastPreviewTask, task -> task.cancel(false));

      Callable<Void> runnable =
          () -> {
            long start = System.currentTimeMillis();
            retrieveInlineCompletion(editor, completionState, startOffset);
            long end = System.currentTimeMillis();
            Thread.sleep(Math.max(0, DEBOUNCE_MILLIS - (end - start)));
            if (editor.getDocument().getModificationStamp() != lastModified) {
              return null;
            }
            ApplicationManager.getApplication()
                .invokeLater(
                    () -> {
                      completionState.resetStats();
                      showInlineCompletion(
                          editor,
                          file,
                          completionState,
                          startOffset,
                          (completion) -> afterCompletionShown(completion, document));
                    },
                    (Condition<Void>)
                        unused -> editor.getDocument().getModificationStamp() != lastModified);
            return null;
          };
      lastPreviewTask = AppExecutorUtil.getAppExecutorService().submit(runnable);
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
    List<TabNineCompletion> result = new ArrayList<>();
    for (int index = 0;
        index < completions.results.length
            && index
                < CompletionUtils.completionLimit(
                    document, completions.old_prefix, offset, completions.is_locked);
        index++) {
      TabNineCompletion completion =
          CompletionUtils.createTabnineCompletion(
              document,
              offset,
              completions.old_prefix,
              completions.results[index],
              index,
              completions.snippet_intent);

      result.add(completion);
    }

    return result;
  }
}
