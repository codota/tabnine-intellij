package com.tabnine.general;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.tabnine.inline.CompletionPreview;
import com.tabnine.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;

public class TabninePopupInhibitor extends TypedHandlerDelegate {

  @NotNull
  public TypedHandlerDelegate.Result checkAutoPopup(final char charTyped, @NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
    TabNineCompletion currentCompletion = CompletionPreview.getCurrentCompletion(editor);
    if (currentCompletion != null) {
      return Result.STOP;
    }
    return super.checkAutoPopup(charTyped, project, editor, file);
  }
}
