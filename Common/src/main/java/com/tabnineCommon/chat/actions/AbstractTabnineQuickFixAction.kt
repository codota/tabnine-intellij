package com.tabnineCommon.chat.actions

import com.intellij.application.subscribe
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import com.intellij.util.messages.Topic
import com.tabnineCommon.chat.actions.common.ChatActionCommunicator
import com.tabnineCommon.general.StaticConfig
import javax.swing.Icon

abstract class AbstractTabnineQuickFixAction : PsiElementBaseIntentionAction(), IntentionAction, Disposable, Iconable {
    // `ChatActionCommunicator.sendMessageToChat` requires an EDT thread, but the `invoke` method of `IntentionAction`
    // is not called from the EDT thread.
    // Also, when calling `ApplicationManager.getApplication().invokeLater` directly in the `invoke` implementation,
    // you get this error: `Side effect not allowed: INVOKE_LATER`.
    //
    // To work around this issue, we use an internal topic that'll call `ApplicationManager.getApplication().invokeLater`
    // in it's own context (thread), circumventing the issue.
    private val workaroundTopic = Topic.create("TabnineQuickFixAction", WorkaroundHandler::class.java)

    companion object {
        private const val FAMILY_NAME: String = "Fix using Tabnine"
        private const val ID: String = "com.tabnine.chat.actions.TabnineQuickFixAction"
    }

    init {
        workaroundTopic.subscribe(
            this,
            object : WorkaroundHandler {
                override fun handle(project: Project, value: String) {
                    ApplicationManager.getApplication().invokeLater {
                        ChatActionCommunicator.sendMessageToChat(project, ID, value)
                    }
                }
            }
        )
    }

    override fun getText(): String {
        return FAMILY_NAME
    }

    override fun getFamilyName(): String {
        return FAMILY_NAME
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (!isChatEnabled()) return false

        if (editor == null) return false
        val textRangeToSearch = getSelectedRange(editor) ?: element.textRange
        var foundFixes = false
        DaemonCodeAnalyzerImpl.processHighlights(
            editor.document,
            project,
            HighlightSeverity.WARNING,
            textRangeToSearch.startOffset,
            textRangeToSearch.endOffset,
            Processor {
                foundFixes = true
                return@Processor false
            }
        )

        return foundFixes
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        ApplicationManager
            .getApplication()
            .messageBus
            .syncPublisher(workaroundTopic)
            .handle(project, "/fix-code")
    }

    private fun getSelectedRange(editor: Editor): TextRange? {
        val selectionModel = editor.selectionModel
        return if (selectionModel.hasSelection()) {
            TextRange(selectionModel.selectionStart, selectionModel.selectionEnd)
        } else {
            null
        }
    }

    override fun dispose() {
    }

    override fun getIcon(flags: Int): Icon {
        return StaticConfig.getTabnineIcon()
    }

    abstract fun isChatEnabled(): Boolean
}

interface WorkaroundHandler {
    fun handle(project: Project, value: String)
}
