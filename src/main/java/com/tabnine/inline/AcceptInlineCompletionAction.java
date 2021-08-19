package com.tabnine.inline;

import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AcceptInlineCompletionAction extends EditorAction
    implements HintManagerImpl.ActionToIgnore, InlineCompletionAction {

  public static final String ACTION_ID = "AcceptInlineCompletionAction";

  public AcceptInlineCompletionAction() {
    super(new AcceptInlineCompletionHandler());
  }

  public static class AcceptInlineCompletionHandler extends EditorActionHandler {

    @Override
    protected void doExecute(
        @NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
      CompletionPreview completionPreview = CompletionPreview.findCompletionPreview(editor);
      if (completionPreview == null) {
        return;
      }
      completionPreview.applyPreview();
    }
  }
}
