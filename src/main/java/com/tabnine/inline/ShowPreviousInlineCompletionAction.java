package com.tabnine.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class ShowPreviousInlineCompletionAction extends BaseCodeInsightAction
    implements DumbAware, InlineCompletionAction {
  public static final String ACTION_ID = "ShowPreviousInlineCompletionAction";

  public ShowPreviousInlineCompletionAction() {
    super(false);
  }

  @Override
  protected @NotNull CodeInsightActionHandler getHandler() {
    return (project, editor, file) -> {
      CompletionPreview completionPreview = CompletionPreview.getInstance(editor);

      if (completionPreview != null) {
        completionPreview.togglePreview(CompletionOrder.PREVIOUS);
      }
    };
  }

  @Override
  protected boolean isValidForLookup() {
    return true;
  }
}
