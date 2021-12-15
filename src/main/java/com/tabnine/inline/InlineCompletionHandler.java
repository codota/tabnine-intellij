package com.tabnine.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.completion.CompletionData;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.DocumentUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBus;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.general.DependencyContainer;
import com.tabnine.intellij.completions.CompletionUtils;
import com.tabnine.intellij.completions.LimitedSecletionsChangedNotifier;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.prediction.TabNinePrefixMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.tabnine.inline.CompletionPreview.MUTE_CARET_LISTENER;

public class InlineCompletionHandler implements CodeInsightActionHandler {
  private static final String INLINE_DUMMY_IDENTIFIER = "TabnineInlineDummy";
  private static final Set<Character> CLOSING_CHARACTERS = ContainerUtil.set('\'', '"', '`', ']', '}', ')', '>');

  private final CompletionFacade completionFacade =
      DependencyContainer.instanceOfCompletionFacade();
  private final MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
  private final boolean myForward;
  private static boolean isLocked;

  InlineCompletionHandler(boolean forward) {
    this.myForward = forward;
  }

  void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, int offset) {
    Document document = editor.getDocument();

    // if we cannot modify this file, return
    if (editor.isViewer() || document.getRangeGuard(offset, offset) != null) {
      document.fireReadOnlyModificationAttempt();
      EditorModificationUtil.checkModificationAllowed(editor);
      return;
    }

    if (isInTheMiddleOftWord(document, offset)) {
      return;
    }

    // data about previous completions, or just a new fresh state for the editor
    final CompletionState completionState = CompletionState.findOrCreateCompletionState(editor);
    int lastDisplayedCompletionIndex = completionState.lastDisplayedCompletionIndex;

    boolean noOldSuggestion = lastDisplayedCompletionIndex == -1 || completionState.prefix == null;
    boolean editorLocationHasChanged = completionState.lastStartOffset != offset;
    boolean documentChanged =
        completionState.lastModificationStamp != document.getModificationStamp();

    if (noOldSuggestion || editorLocationHasChanged || documentChanged) {
      // start a new query
      completionState.prefix = computeCurrentPrefix(editor, project, file, offset);
      completionState.lastDisplayedCompletionIndex = -1;
      retrieveAndShowInlineCompletion(editor, file, completionState, offset);
    } else {
      showInlineCompletion(editor, file, completionState, completionState.lastStartOffset);
    }
  }

  private boolean isInTheMiddleOftWord(@NotNull Document document, int offset) {
    if (DocumentUtil.isAtLineEnd(offset, document)) {
      return false;
    }
    char nextChar = document.getText(new TextRange(offset, offset + 1)).charAt(0);
    return !CLOSING_CHARACTERS.contains(nextChar) && !Character.isWhitespace(nextChar);
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    int caretOffset = editor.getCaretModel().getOffset();
    invoke(project, editor, file, caretOffset);
  }

  private String computeCurrentPrefix(
      @NotNull Editor editor, @NotNull Project project, @NotNull PsiFile file, int offset) {
    Document document = editor.getDocument();
    String documentText = document.getText();
    if (offset < 0 || offset > documentText.length()) {
      return "";
    }
    documentText =
        new StringBuilder(documentText).insert(offset, INLINE_DUMMY_IDENTIFIER).toString();

    file =
        PsiFileFactory.getInstance(project)
            .createFileFromText("tmp-" + file.getName(), file.getFileType(), documentText);
    PsiElement element = file.findElementAt(offset);
    if (element == null) {
      return "";
    }
    return CompletionData.findPrefixStatic(element, offset);
  }

  private void showInlineCompletion(
      @NotNull Editor editor,
      @NotNull PsiFile file,
      CompletionState completionState,
      int startOffset) {
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

    editor.putUserData(MUTE_CARET_LISTENER, false);

    if (completionState.lastDisplayedPreview != null &&
            completionState.lastDisplayedPreview.endsWith(nextSuggestion.getSuffix())) {
      editor.putUserData(MUTE_CARET_LISTENER, true);
    }

    CompletionPreview preview = CompletionPreview.findOrCreateCompletionPreview(editor, file);
    completionState.lastDisplayedPreview =
        preview.updatePreview(completionState.suggestions, nextIndex, startOffset);
    completionState.lastDisplayedCompletionIndex = nextIndex;
    completionState.lastStartOffset = startOffset;
    completionState.lastModificationStamp = editor.getDocument().getModificationStamp();
  }

  private void retrieveInlineCompletion(
      @NotNull Document document, CompletionState completionState, int startOffset) {
    AutocompleteResponse completionsResponse =
        this.completionFacade.retrieveCompletions(document, startOffset);

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
        createCompletions(completionsResponse, document, completionState.prefix, startOffset);
  }

  private void retrieveAndShowInlineCompletion(
      @NotNull Editor editor,
      @NotNull PsiFile file,
      CompletionState completionState,
      int startOffset) {
    final Document document = editor.getDocument();
    final long lastModified = document.getModificationStamp();

    final Runnable retrieveCompletionsTask =
        () -> retrieveInlineCompletion(document, completionState, startOffset);
    final Runnable afterCompletionsRunner =
        () -> {
          completionState.resetStats(editor);
          showInlineCompletion(editor, file, completionState, startOffset);
        };
    final Consumer<Void> completionsConsumer = val -> afterCompletionsRunner.run();

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      retrieveCompletionsTask.run();
      afterCompletionsRunner.run();
    } else {
      ReadAction.nonBlocking(retrieveCompletionsTask)
          .expireWhen(() -> editor.isDisposed() || document.getModificationStamp() != lastModified)
          .finishOnUiThread(ModalityState.NON_MODAL, completionsConsumer)
          .submit(AppExecutorUtil.getAppExecutorService());
    }
  }

  private List<TabNineCompletion> createCompletions(
      AutocompleteResponse completions,
      @NotNull Document document,
      @NotNull String prefix,
      int offset) {
    List<TabNineCompletion> result = new ArrayList<>();
    PrefixMatcher prefixMatcher = new TabNinePrefixMatcher(new PlainPrefixMatcher(prefix));
    for (int index = 0;
        index < completions.results.length
            && index
                < CompletionUtils.completionLimit(document, prefix, offset, completions.is_locked);
        index++) {
      TabNineCompletion completion =
          CompletionUtils.createTabnineCompletion(
              document, prefix, offset, completions.old_prefix, completions.results[index], index);

      if (prefixMatcher.prefixMatches(completion.newPrefix)) {
        result.add(completion);
      }
    }

    return result;
  }
}
