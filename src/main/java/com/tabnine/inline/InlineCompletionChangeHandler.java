package com.tabnine.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class InlineCompletionChangeHandler implements CodeInsightActionHandler {
  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {}
}
