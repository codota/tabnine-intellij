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
        return "Tabnine"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return settingsComponent!!.preferredFocusedComponent
    }

    override fun createComponent(): JComponent {
        settingsComponent = AppSettingsComponent()
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        if (isValidForm()) {
            val settings = instance
            settingsComponent?.let {
                return it.chosenColor != settings.inlineHintColor ||
                    it.useDefaultColor != settings.useDefaultColor ||
                    it.logFilePath != settings.logFilePath ||
                    it.logLevel != settings.logLevel ||
                    it.debounceTime != settings.debounceTime.toString() ||
                    it.autoImportEnabled != settings.autoImportEnabled
            }
        }
        return false
    }

    override fun apply() {
        if (isValidForm()) {
            val settings = instance
            settings.inlineHintColor = settingsComponent!!.chosenColor
            settings.useDefaultColor = settingsComponent!!.useDefaultColor
            settings.logFilePath = settingsComponent!!.logFilePath
            settings.logLevel = settingsComponent!!.logLevel
            settings.debounceTime = settingsComponent!!.debounceTime.toLong()
            settings.autoImportEnabled = settingsComponent!!.autoImportEnabled
        }
    }

    override fun reset() {
        val settings = instance
        settingsComponent?.let {
            it.chosenColor = settings.inlineHintColor
            it.useDefaultColor = settings.useDefaultColor
            it.logFilePath = settings.logFilePath
            it.logLevel = settings.logLevel
            it.debounceTime = settings.debounceTime.toString()
            it.autoImportEnabled = settings.autoImportEnabled
        }
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }

    private fun isValidForm(): Boolean {
        settingsComponent?.let {
            val debounceTime = it.debounceTime.toLongOrNull()
            return debounceTime != null && debounceTime >= 0
        }
        return false
    }
}
