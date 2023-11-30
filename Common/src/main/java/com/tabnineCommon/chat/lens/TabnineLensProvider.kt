package com.tabnineCommon.chat.lens

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.InlayPresentationFactory
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.startOffset
import com.tabnineCommon.chat.actions.common.ChatActionCommunicator
import com.tabnineCommon.general.StaticConfig
import java.awt.Point
import java.awt.event.MouseEvent

internal class TabnineLensProvider(private val supportedElementTypes: List<String>) : InlayHintsProvider<NoSettings> {
    companion object {
        private const val ID = "com.tabnine.chat.lens"
    }

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ) = MyCollector(editor, supportedElementTypes)

    class MyCollector(
        private val editor: Editor,
        private val supportedElementTypes: List<String>
    ) : FactoryInlayHintsCollector(editor) {

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            if (element.elementType.toString() in supportedElementTypes) {
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

        private fun handleActionClick(editor: Editor, element: PsiElement, command: String) {
            val selectionModel = editor.selectionModel
            val range = element.textRange
            selectionModel.setSelection(range.startOffset, range.endOffset)

            ChatActionCommunicator.sendMessageToChat(editor.project!!, ID, command)
        }

        private fun buildQuickActionItem(label: String, command: String, editor: Editor, element: PsiElement, includeSeparator: Boolean): InlayPresentation {
            return factory.seq(
                factory.smallText(" "),
                factory.smallText(if (includeSeparator) "| " else ""),
                factory.referenceOnHover(
                    factory.smallText(label),
                    object : InlayPresentationFactory.ClickListener {
                        override fun onClick(event: MouseEvent, translated: Point) {
                            handleActionClick(editor, element, command)
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

    override val key: SettingsKey<NoSettings> = SettingsKey("tabnine.chat.inlay.provider")

    override val name: String = "Tabnine chat actions"

    override val previewText: String? = null

    override fun createSettings(): NoSettings = NoSettings()

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        TODO("Not yet implemented")
    }
}
