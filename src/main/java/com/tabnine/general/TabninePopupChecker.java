package com.tabnine.general;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.tabnine.inline.CompletionPreview;
import com.tabnine.inline.InlineCompletionCache;
import org.jetbrains.annotations.NotNull;

public class TabninePopupChecker extends TypedHandlerDelegate {

  @NotNull
  public TypedHandlerDelegate.Result checkAutoPopup(
      final char charTyped,
      @NotNull final Project project,
      @NotNull final Editor editor,
      @NotNull final PsiFile file) {
    boolean hasCompletionPreview = CompletionPreview.getCurrentCompletion(editor) != null;
    boolean hasRelevantCache = !InlineCompletionCache.getInstance().retrieveAdjustedCompletions(editor, String.valueOf(charTyped)).isEmpty();
    if (hasCompletionPreview || hasRelevantCache) {
      return Result.STOP;
    }
    return super.checkAutoPopup(charTyped, project, editor, file);
  }
}
