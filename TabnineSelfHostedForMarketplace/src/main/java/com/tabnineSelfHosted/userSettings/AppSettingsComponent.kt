package com.tabnineSelfHosted.userSettings

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

/**
 * Supports creating and managing a [JPanel] for the Settings Dialog.
 */
class AppSettingsComponent {
    val panel: JPanel
    private val cloud2UrlComponent = JBTextField()

    var cloud2Url: String
        get() = cloud2UrlComponent.text
        set(value) {
            cloud2UrlComponent.text = value
        }
    init {

        val panelBuilder = FormBuilder.createFormBuilder()
            .addLabeledComponent("Tabnine Enterprise URL (requires restart): ", cloud2UrlComponent, 1, true)
        panel = panelBuilder.panel
    }
}
