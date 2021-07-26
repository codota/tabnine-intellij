package com.tabnine.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class ShowNextInlineCompletionAction extends BaseCodeInsightAction implements DumbAware {

    static final String ACTION_ID = "ShowNextInlineCompletionAction";

    public ShowNextInlineCompletionAction() {
        super(false);
    }

    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return new InlineCompletionHandler(true);
    }

    @Override
    protected boolean isValidForLookup() {
        return true;
    }
}
