package com.tabnineCommon.inline

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class ShowPreviousTabnineInlineCompletionAction :
    BaseCodeInsightAction(false),
    DumbAware,
    InlineCompletionAction {
    companion object {
        const val ACTION_ID = "ShowPreviousTabnineInlineCompletionAction"
    }

    override fun getHandler(): CodeInsightActionHandler {
        return CodeInsightActionHandler { _: Project?, editor: Editor, _: PsiFile? ->
            CompletionPreview.getInstance(editor)?.togglePreview(CompletionOrder.PREVIOUS)
        }
    }

    override fun isValidForLookup(): Boolean = true
}
