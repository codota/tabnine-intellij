package com.tabnineCommon.userSettings

import com.intellij.openapi.options.Configurable
import com.tabnineSelfHosted.userSettings.AppSettingsState.Companion.instance
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
        return "Tabnine"
    }

    override fun createComponent(): JComponent? {
        settingsComponent = AppSettingsComponent()
        return settingsComponent?.panel
    }

    override fun isModified(): Boolean {
        val settings = instance
        settingsComponent?.let {
            return it.cloud2Url != settings.cloud2Url
        }
        return false
    }

    override fun apply() {
        val settings = instance
        settings.cloud2Url = settingsComponent!!.cloud2Url
    }

    override fun reset() {
        val settings = instance
        settingsComponent?.let {
            it.cloud2Url = settings.cloud2Url
        }
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
