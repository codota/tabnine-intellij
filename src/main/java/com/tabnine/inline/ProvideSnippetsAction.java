package com.tabnine.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class ProvideSnippetsAction extends BaseCodeInsightAction
    implements DumbAware, InlineCompletionAction {

  public static final String ACTION_ID = "ProvideSnippetsAction";

  public ProvideSnippetsAction() {
    super(false);
  }

  @Override
  protected @NotNull CodeInsightActionHandler getHandler() {
    return new InlineCompletionHandler(true, true);
  }

  @Override
  protected boolean isValidForLookup() {
    return true;
  }
}
