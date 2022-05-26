package com.tabnine.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class ShowNextInlineCompletionAction extends BaseCodeInsightAction
    implements DumbAware, InlineCompletionAction {

  public static final String ACTION_ID = "ShowNextInlineCompletionAction";

  public ShowNextInlineCompletionAction() {
    super(false);
  }

  @Override
  protected @NotNull CodeInsightActionHandler getHandler() {
    return (project, editor, file) -> {
      CompletionPreview completionPreview = CompletionPreview.getInstance(editor);

      if (completionPreview != null) {
        completionPreview.togglePreview(CompletionOrder.NEXT);
      }
    };
  }

  @Override
  protected boolean isValidForLookup() {
    return true;
  }
}
