package com.tabnine.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class ShowPreviousInlineCompletionAction extends BaseCodeInsightAction implements DumbAware {

  public static final String ACTION_ID = "ShowPreviousInlineCompletionAction";

  public ShowPreviousInlineCompletionAction() {
    super(false);
  }

  @Override
  protected @NotNull CodeInsightActionHandler getHandler() {
    return new InlineCompletionHandler(false);
  }

  @Override
  protected boolean isValidForLookup() {
    return true;
  }
}
