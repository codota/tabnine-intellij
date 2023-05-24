package com.tabnineCommon.userSettings

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.tabnineCommon.general.Utils.replaceCustomRepository
import com.tabnineCommon.inline.render.GraphicsUtils

val settingsDefaultColor = GraphicsUtils.niceContrastColor.rgb

const val PROPERTIES_COMPONENT_NAME = "com.tabnine.enterprise-url"

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
    var cloud2Url: String = getCloudUrlImpl()
        set(value) {
            replaceCustomRepository(field, value)
            PropertiesComponent.getInstance().setValue(PROPERTIES_COMPONENT_NAME, value)
            field = value.trim()
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
        private fun getCloudUrlImpl(): String {
            val current = PropertiesComponent.getInstance().getValue(PROPERTIES_COMPONENT_NAME)
            if (current.isNullOrBlank()) return ""
            return current
        }

        @JvmStatic
        val instance: AppSettingsState
            get() = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }
}
