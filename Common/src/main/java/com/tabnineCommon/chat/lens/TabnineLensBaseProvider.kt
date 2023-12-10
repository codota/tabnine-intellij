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

open class TabnineLensBaseProvider(private val supportedElementTypes: List<String>) : InlayHintsProvider<NoSettings> {
    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ) = TabnineLensCollector(editor, supportedElementTypes)

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

open class TabnineLensJavaProvider : TabnineLensBaseProvider(listOf("CLASS", "METHOD"))
open class TabnineLensPythonProvider : TabnineLensBaseProvider(listOf("Py:CLASS_DECLARATION", "Py:FUNCTION_DECLARATION"))
open class TabnineLensTypescriptProvider : TabnineLensBaseProvider(listOf("JS:FUNCTION_DECLARATION", "JS:ES6_CLASS", "JS:CLASS", "JS:TYPESCRIPT_FUNCTION", "JS:TYPESCRIPT_CLASS"))
open class TabnineLensKotlinProvider : TabnineLensBaseProvider(listOf("CLASS", "FUN"))
open class TabnineLensPhpProvider : TabnineLensBaseProvider(listOf("Class", "Class method", "Function"))
open class TabnineLensRustProvider : TabnineLensBaseProvider(listOf("FUNCTION"))
