package com.tabnineSelfHosted.userSettings

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.tabnineCommon.general.Utils

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
    var cloud2Url: String = getCloudUrlImpl()
        set(value) {
            Utils.replaceCustomRepository(field, value)
            PropertiesComponent.getInstance().setValue(com.tabnineCommon.userSettings.PROPERTIES_COMPONENT_NAME, value)
            field = value.trim()
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
            val current = PropertiesComponent.getInstance().getValue(com.tabnineCommon.userSettings.PROPERTIES_COMPONENT_NAME)
            if (current.isNullOrBlank()) return ""
            return current
        }

        @JvmStatic
        val instance: AppSettingsState
            get() = ServiceManager.getService(AppSettingsState::class.java)
    }
}
