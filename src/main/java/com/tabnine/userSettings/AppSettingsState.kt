package com.tabnine.userSettings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import com.intellij.util.xmlb.XmlSerializerUtil
import com.tabnine.inline.render.GraphicsUtils

val settingsDefaultColor = GraphicsUtils.niceContrastColor.rgb

/**
 * This package (`userSettings`) is heavily influenced by the docs from here:
 * https://plugins.jetbrains.com/docs/intellij/settings-tutorial.html
 *
 *
 * Supports storing the application settings in a persistent way.
 * The [State] and [Storage] annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(name = "com.tabnine.userSettings.AppSettingsState", storages = [Storage("TabnineSettings.xml")])
class AppSettingsState : PersistentStateComponent<AppSettingsState?> {
    var useDefaultColor: Boolean = false
    var logFilePath: String = ""
    var logLevel: String = ""
    var debounceTime: Long = 0
    var autoImportEnabled: Boolean = true
    var binariesFolderOverride: String = ""
    var cloud2Url: String = ""
        set(value) {
            val hostsList = UpdateSettings.getInstance().storedPluginHosts
            val newStore = "${value.trimEnd('/', ' ')}/update/jetbrains/updatePlugins.xml"
            val oldStore = "${field.trimEnd('/', ' ')}/update/jetbrains/updatePlugins.xml"
            if (value.isNotBlank() && !hostsList.contains(newStore)) {
                hostsList.add(newStore)
            } else if (field.isNotBlank() && newStore != oldStore) {
                hostsList.remove(oldStore)
            }
            field = value
        }
    var useIJProxySettings: Boolean = true

    private var colorState = settingsDefaultColor

    var inlineHintColor: Int
        get() = if (useDefaultColor) {
            settingsDefaultColor
        } else {
            colorState
        }
        set(value) {
            colorState = value
        }

    override fun getState(): AppSettingsState {
        return this
    }

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        val instance: AppSettingsState
            get() = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }
}
