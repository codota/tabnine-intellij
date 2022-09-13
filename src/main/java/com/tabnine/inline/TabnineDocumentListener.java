package com.tabnine.inline;

import static com.intellij.openapi.editor.EditorModificationUtil.checkModificationAllowed;
import static com.tabnine.general.DependencyContainer.instanceOfSuggestionsModeService;
import static com.tabnine.general.DependencyContainer.singletonOfInlineCompletionHandler;

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
import com.tabnine.capabilities.SuggestionsModeService;
import com.tabnine.general.EditorUtils;
import java.awt.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineDocumentListener implements BulkAwareDocumentListener {
  private final InlineCompletionHandler handler = singletonOfInlineCompletionHandler();
  private final SuggestionsModeService suggestionsModeService = instanceOfSuggestionsModeService();

  @Override
  public void documentChangedNonBulk(@NotNull DocumentEvent event) {
    Document document = event.getDocument();
    Editor editor = getActiveEditor(document);

    if (editor == null || !EditorUtils.isMainEditor(editor)) {
      return;
    }

    CompletionPreview.clear(editor);

    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              int offset =
                  editor.getCaretModel().getOffset()
                      + (ApplicationManager.getApplication().isUnitTestMode()
                          ? event.getNewLength()
                          : 0);

              if (shouldIgnoreChange(event, editor, offset)) {
                return;
              }

              handler.retrieveAndShowCompletion(editor, offset);
            });
  }

  private boolean shouldIgnoreChange(DocumentEvent event, Editor editor, int offset) {
    Document document = event.getDocument();

    if (event.getNewLength() < 1 || !suggestionsModeService.getSuggestionMode().isInlineEnabled()) {
      return true;
    }

    if (!editor.getEditorKind().equals(EditorKind.MAIN_EDITOR)
        && !ApplicationManager.getApplication().isUnitTestMode()) {
      return true;
    }

    if (!checkModificationAllowed(editor) || document.getRangeGuard(offset, offset) != null) {
      document.fireReadOnlyModificationAttempt();

      return true;
    }

    if (!CompletionUtils.isValidMidlinePosition(document, offset)) {
      return true;
    }

    return isInTheMiddleOfWord(document, offset);
  }

  private boolean isInTheMiddleOfWord(@NotNull Document document, int offset) {
    try {
      if (DocumentUtil.isAtLineEnd(offset, document)) {
        return false;
      }

      char nextChar = document.getText(new TextRange(offset, offset + 1)).charAt(0);
      return Character.isLetterOrDigit(nextChar) || nextChar == '_' || nextChar == '-';
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
