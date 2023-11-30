package com.tabnineCommon.chat.lens

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.InlayPresentationFactory
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.startOffset
import com.tabnineCommon.binary.requests.analytics.EventRequest
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.chat.actions.common.ChatActionCommunicator
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.StaticConfig
import java.awt.Point
import java.awt.event.MouseEvent

class TabnineLensCollector(
    editor: Editor,
    private val enabledElementTypes: List<String>
) : FactoryInlayHintsCollector(editor) {
    companion object {
        private const val ID = "com.tabnine.chat.lens"
    }

    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        if (!CapabilitiesService.getInstance().isCapabilityEnabled(Capability.TABNINE_CHAT)) {
            return false
        }
        if (element.elementType.toString() in enabledElementTypes) {
            sink.addBlockElement(
                offset = element.startOffset,
                relatesToPrecedingText = true,
                showAbove = true,
                priority = 0,
                presentation = factory.seq(
                    factory.textSpacePlaceholder(countLeadingWhitespace(editor, element), false),
                    factory.icon(StaticConfig.getTabnineLensIcon()),
                    buildQuickActionItem("Explain", "/explain-code", editor, element, false),
                    buildQuickActionItem("Test", "/generate-test-for-code", editor, element, true),
                    buildQuickActionItem("Document", "/document-code", editor, element, true),
                    buildQuickActionItem("Fix", "/fix-code", editor, element, true),
                    buildAskActionItem("Ask", editor, element),
                )
            )
        }
        return true
    }

    private fun buildQuickActionItem(label: String, intent: String, editor: Editor, element: PsiElement, includeSeparator: Boolean): InlayPresentation {
        return factory.seq(
            factory.smallText(" "),
            factory.smallText(if (includeSeparator) "| " else ""),
            factory.referenceOnHover(
                factory.smallText(label),
                object : InlayPresentationFactory.ClickListener {
                    override fun onClick(event: MouseEvent, translated: Point) {
                        sendClickEvent(intent)

                        val selectionModel = editor.selectionModel
                        val range = element.textRange
                        selectionModel.setSelection(range.startOffset, range.endOffset)

                        ChatActionCommunicator.sendMessageToChat(editor.project!!, ID, intent)
                    }
                },
            )
        )
    }

    private fun buildAskActionItem(label: String, editor: Editor, element: PsiElement): InlayPresentation {
        return factory.seq(
            factory.smallText(" "),
            factory.smallText("| "),
            factory.referenceOnHover(
                factory.smallText(label),
                object : InlayPresentationFactory.ClickListener {
                    override fun onClick(event: MouseEvent, translated: Point) {
                        sendClickEvent("ask")

                        val result =
                            Messages.showInputDialog("How can I assist with this code?", "Ask Tabnine", StaticConfig.getTabnineIcon())
                                .takeUnless { it.isNullOrBlank() }
                                ?: return

                        val selectionModel = editor.selectionModel
                        val range = element.textRange
                        selectionModel.setSelection(range.startOffset, range.endOffset)

                        ChatActionCommunicator.sendMessageToChat(editor.project!!, ID, result)
                    }
                },
            )
        )
    }

    private fun sendClickEvent(intent: String) {
        binaryRequestFacade.executeRequest(
            EventRequest(
                "chat-code-lens-click",
                mapOf("intent" to intent)
            )
        )
    }

    private fun countLeadingWhitespace(editor: Editor, element: PsiElement): Int {
        val lineNumber = editor.document.getLineNumber(element.startOffset)
        return editor.document.getText(
            TextRange(
                editor.document.getLineStartOffset(lineNumber),
                editor.document.getLineEndOffset(lineNumber)
            )
        ).takeWhile { it.isWhitespace() }.length
    }
}
