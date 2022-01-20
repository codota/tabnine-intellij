package com.tabnine.userSettings

import com.intellij.openapi.options.Configurable
import com.tabnine.userSettings.AppSettingsState.Companion.instance
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class AppSettingsConfigurable : Configurable {
    private var settingsComponent: AppSettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Tabnine: Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return settingsComponent!!.preferredFocusedComponent
    }

    override fun createComponent(): JComponent {
        settingsComponent = AppSettingsComponent()
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = instance
        return settingsComponent!!.chosenColor != settings.color
    }

    override fun apply() {
        val settings = instance
        settings.color = settingsComponent!!.chosenColor
    }

    override fun reset() {
        val settings = instance
        settingsComponent!!.chosenColor = settings.color
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
