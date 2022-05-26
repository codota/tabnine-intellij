package com.tabnine.inline;

import static com.intellij.openapi.editor.EditorModificationUtil.checkModificationAllowed;
import static com.tabnine.general.DependencyContainer.singletonOfInlineCompletionHandler;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.util.DocumentUtil;
import com.intellij.util.containers.ContainerUtil;
import com.tabnine.capabilities.SuggestionsMode;
import java.awt.*;
import java.util.Arrays;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineDocumentListener implements BulkAwareDocumentListener {
  private static final Set<Character> CLOSING_CHARACTERS =
      ContainerUtil.set('\'', '"', '`', ']', '}', ')', '>');
  private static final java.util.List<String> AUTO_FILLING_PAIRS =
      Arrays.asList("()", "{}", "[]", "''", "\"\"", "``");

  private final InlineCompletionHandler handler = singletonOfInlineCompletionHandler();

  @Override
  public void documentChangedNonBulk(@NotNull DocumentEvent event) {
    Document document = event.getDocument();
    Editor editor = getActiveEditor(document);

    if (editor == null) {
      return;
    }

    CompletionPreview.clear(editor);

    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              if (shouldIgnoreChange(event, editor)) {
                Logger.getInstance(getClass()).warn("BOAZ: DocumentChanged. Ignoring change");
                return;
              }

              handler.retrieveAndShowCompletion(editor);
            });
  }

  private boolean shouldIgnoreChange(DocumentEvent event, Editor editor) {
    Document document = event.getDocument();

    if (SuggestionsMode.getSuggestionMode() != SuggestionsMode.INLINE || event.getNewLength() < 1) {
      return true;
    }

    if (newTextIsAutoFilled(event)) {
      return true;
    }

    if (editor == null
        || (!editor.getEditorKind().equals(EditorKind.MAIN_EDITOR)
            && !ApplicationManager.getApplication().isUnitTestMode())) {
      return true;
    }

    int offset = editor.getCaretModel().getOffset();

    if (!checkModificationAllowed(editor) || document.getRangeGuard(offset, offset) != null) {
      document.fireReadOnlyModificationAttempt();

      return true;
    }

    if (isInTheMiddleOfWord(document, offset)) {
      return true;
    }

    return false;
  }

  // counts `\n\t` as a single change too.
  private boolean newTextIsSingleChange(String newText) {
    return newText.length() == 1 || newText.trim().isEmpty();
  }

  private boolean newTextIsAutoFilled(@NotNull DocumentEvent event) {
    String eventNewText = event.getNewFragment().toString();

    if (CompletionUtil.DUMMY_IDENTIFIER.equals(eventNewText)
        || !newTextIsSingleChange(eventNewText)) {
      return true;
    }

    try {
      int endOffset = event.getOffset() + event.getNewLength();
      String textIncludingPreviousChar =
          event.getDocument().getText(new TextRange(event.getOffset() - 1, endOffset));

      return AUTO_FILLING_PAIRS.contains(textIncludingPreviousChar)
          || AUTO_FILLING_PAIRS.contains(eventNewText);
    } catch (Throwable e) {
      Logger.getInstance(getClass())
          .debug("Could not determine if document change is auto filled, skipping: ", e);
    }

    return false;
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

  @Nullable
  private static Editor getActiveEditor(@NotNull Document document) {
    if (!ApplicationManager.getApplication().isDispatchThread()) {
      return null;
    }

    Component focusOwner = IdeFocusManager.getGlobalInstance().getFocusOwner();
    DataContext dataContext = DataManager.getInstance().getDataContext(focusOwner);
    // ignore caret placing when exiting
    Editor activeEditor =
        ApplicationManager.getApplication().isDisposed()
            ? null
            : CommonDataKeys.EDITOR.getData(dataContext);

    if (activeEditor != null && activeEditor.getDocument() != document) {
      activeEditor = null;
    }

    return activeEditor;
  }
}
