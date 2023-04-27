package com.tabnineCommon.inline;

import static com.intellij.openapi.editor.EditorModificationUtil.checkModificationAllowed;
import static com.tabnineCommon.general.Utils.isUnitTestMode;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.wm.IdeFocusManager;
import com.tabnineCommon.binary.requests.notifications.shown.SuggestionDroppedReason;
import com.tabnineCommon.capabilities.ISuggestionsModeService;
import com.tabnineCommon.general.CompletionsEventSender;
import com.tabnineCommon.general.EditorUtils;
import com.tabnineCommon.general.IProviderOfThings;
import com.tabnineCommon.prediction.TabNineCompletion;
import java.awt.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineDocumentListener implements BulkAwareDocumentListener {
  private final InlineCompletionHandler handler =
      ServiceManager.getService(IProviderOfThings.class).getInlineCompletionHandler();
  private final ISuggestionsModeService suggestionsModeService =
      ServiceManager.getService(IProviderOfThings.class).getSuggestionsModeService();
  private final CompletionsEventSender completionsEventSender =
      ServiceManager.getService(IProviderOfThings.class).getCompletionsEventSender();

  @Override
  public void documentChangedNonBulk(@NotNull DocumentEvent event) {
    Document document = event.getDocument();
    Editor editor = getActiveEditor(document);

    if (editor == null || !EditorUtils.isMainEditor(editor)) {
      return;
    }

    TabNineCompletion lastShownCompletion = CompletionPreview.getCurrentCompletion(editor);

    CompletionPreview.clear(editor);

    int offset = event.getOffset() + event.getNewLength();

    if (shouldIgnoreChange(event, editor, offset, lastShownCompletion)) {
      InlineCompletionCache.getInstance().clear(editor);
      return;
    }

    handler.retrieveAndShowCompletion(
        editor,
        offset,
        lastShownCompletion,
        event.getNewFragment().toString(),
        new DefaultCompletionAdjustment());
  }

  private boolean shouldIgnoreChange(
      DocumentEvent event, Editor editor, int offset, TabNineCompletion lastShownCompletion) {
    Document document = event.getDocument();

    if (!suggestionsModeService.getSuggestionMode().isInlineEnabled()) {
      return true;
    }

    if (event.getNewLength() < 1) {
      completionsEventSender.sendSuggestionDropped(
          editor, lastShownCompletion, SuggestionDroppedReason.TextDeletion);
      return true;
    }

    if (!editor.getEditorKind().equals(EditorKind.MAIN_EDITOR) && isUnitTestMode()) {
      return true;
    }

    if (!checkModificationAllowed(editor) || document.getRangeGuard(offset, offset) != null) {
      document.fireReadOnlyModificationAttempt();

      return true;
    }

    return !CompletionUtils.isValidDocumentChange(document, offset, event.getOffset());
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
