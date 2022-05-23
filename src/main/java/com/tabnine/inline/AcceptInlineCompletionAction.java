package com.tabnine.inline;

import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AcceptInlineCompletionAction extends EditorAction
    implements HintManagerImpl.ActionToIgnore, InlineCompletionAction {

  public static final String ACTION_ID = "AcceptInlineCompletionAction";

  public AcceptInlineCompletionAction() {
    super(new AcceptInlineCompletionHandler());
  }

  public static class AcceptInlineCompletionHandler extends EditorWriteActionHandler {

    @Override
    public void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext) {
      CompletionPreview completionPreview = CompletionPreview.getInstance(editor);
      if (completionPreview == null) {
        return;
      }
      completionPreview.applyPreview();
    }

    @Override
    protected boolean isEnabledForCaret(
        @NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
      CompletionPreview completionPreview = CompletionPreview.getInstance(editor);
      return completionPreview != null
          && Objects.equals(caret.getOffset(), completionPreview.getStartOffset());
    }
  }
}
