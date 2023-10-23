package com.tabnineCommon.userSettings

import com.intellij.openapi.options.Configurable
import com.tabnineCommon.UIMessages.Dialogs
import com.tabnineCommon.userSettings.AppSettingsState.Companion.instance
import org.jetbrains.annotations.Nls
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class AppSettingsConfigurable : Configurable {
    private var settingsComponent: AppSettingsComponent? = null
    private val requiresRestart = AtomicBoolean(false)

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Tabnine"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return settingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        settingsComponent = AppSettingsComponent()
        return settingsComponent?.panel
    }

    override fun isModified(): Boolean {
        if (isValidForm()) {
            val settings = instance
            settingsComponent?.let {

                return hasChangesThatRequireRestart(it, settings) ||
                    it.chosenColor != settings.inlineHintColor ||
                    it.useDefaultColor != settings.useDefaultColor ||
                    it.autoImportEnabled != settings.autoImportEnabled
            }
        }
        return false
    }

    private fun hasChangesThatRequireRestart(
        component: AppSettingsComponent,
        settings: AppSettingsState
    ): Boolean {
        return component.logFilePath != settings.logFilePath ||
            component.logLevel != settings.logLevel ||
            component.debounceTime != settings.debounceTime.toString() ||
            component.binariesFolderOverride != settings.binariesFolderOverride ||
            component.cloud2Url != settings.cloud2Url ||
            component.useIJProxySettings != settings.useIJProxySettings ||
            component.autoPluginUpdates != settings.autoPluginUpdates ||
            component.ignoreCertificateErrors != settings.ignoreCertificateErrors
    }

    override fun apply() {
        if (isValidForm()) {
            val settings = instance

            if (hasChangesThatRequireRestart(settingsComponent!!, settings)) {
                requiresRestart.set(true)
            }

            settings.inlineHintColor = settingsComponent!!.chosenColor
            settings.useDefaultColor = settingsComponent!!.useDefaultColor
            settings.logFilePath = settingsComponent!!.logFilePath
            settings.logLevel = settingsComponent!!.logLevel
            settings.debounceTime = settingsComponent!!.debounceTime.toLong()
            settings.autoImportEnabled = settingsComponent!!.autoImportEnabled
            settings.binariesFolderOverride = settingsComponent!!.binariesFolderOverride
            settings.cloud2Url = settingsComponent!!.cloud2Url
            settings.useIJProxySettings = settingsComponent!!.useIJProxySettings
            settings.autoPluginUpdates = settingsComponent!!.autoPluginUpdates
            settings.ignoreCertificateErrors = settingsComponent!!.ignoreCertificateErrors
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
            it.binariesFolderOverride = settings.binariesFolderOverride
            it.cloud2Url = settings.cloud2Url
            it.useIJProxySettings = settings.useIJProxySettings
            it.autoPluginUpdates = settings.autoPluginUpdates
            it.ignoreCertificateErrors = settings.ignoreCertificateErrors
        }
        requiresRestart.set(false)
    }

    override fun disposeUIResources() {
        settingsComponent = null
        if (requiresRestart.getAndSet(false)) {
            Dialogs.showRestartDialog("Tabnine settings changed - Restart your IDE for the changes to take effect")
        }
    }

    private fun isValidForm(): Boolean {
        settingsComponent?.let {
            val debounceTime = it.debounceTime.toLongOrNull()
            return debounceTime != null && debounceTime >= 0
        }
        return false
    }
}
