package com.tabnineCommon.chat.lens

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import javax.swing.JComponent
import javax.swing.JPanel

open class TabnineLensBaseProvider(private val supportedElementTypes: List<String>, private val isChatEnabled: () -> Boolean) : InlayHintsProvider<NoSettings> {
    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ) = TabnineLensCollector(editor, supportedElementTypes, isChatEnabled)

    override val key: SettingsKey<NoSettings> = SettingsKey("tabnine.chat.inlay.provider")

    override val name: String = "Tabnine: chat actions"

    override val previewText: String? = null

    override fun createSettings(): NoSettings = NoSettings()

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent {
                return JPanel()
            }
        }
    }
}

open class TabnineLensJavaBaseProvider(isChatEnabled: () -> Boolean) : TabnineLensBaseProvider(listOf("CLASS", "METHOD"), isChatEnabled)
open class TabnineLensPythonBaseProvider(isChatEnabled: () -> Boolean) : TabnineLensBaseProvider(listOf("Py:CLASS_DECLARATION", "Py:FUNCTION_DECLARATION"), isChatEnabled)
open class TabnineLensTypescriptBaseProvider(isChatEnabled: () -> Boolean) : TabnineLensBaseProvider(listOf("JS:FUNCTION_DECLARATION", "JS:ES6_CLASS", "JS:CLASS", "JS:TYPESCRIPT_FUNCTION", "JS:TYPESCRIPT_CLASS"), isChatEnabled)
open class TabnineLensKotlinBaseProvider(isChatEnabled: () -> Boolean) : TabnineLensBaseProvider(listOf("CLASS", "FUN"), isChatEnabled)
open class TabnineLensPhpBaseProvider(isChatEnabled: () -> Boolean) : TabnineLensBaseProvider(listOf("Class", "Class method", "Function"), isChatEnabled)
open class TabnineLensRustBaseProvider(isChatEnabled: () -> Boolean) : TabnineLensBaseProvider(listOf("FUNCTION"), isChatEnabled)
