package com.tabnineSelfHosted.general

import com.intellij.openapi.application.ApplicationManager
import com.tabnineSelfHosted.userSettings.AppSettingsState

object StaticConfig {
    private const val TABNINE_ENTERPRISE_HOST = "TABNINE_ENTERPRISE_HOST"
    const val TABNINE_ENTERPRISE_ID_RAW = "com.tabnine.TabNine-Enterprise"

    @JvmStatic
    fun getTabnineEnterpriseHost(): String? {
        val path = ApplicationManager.getApplication().getService(AppSettingsState::class.java).cloud2Url
        return path.ifEmpty {
            System.getProperty(TABNINE_ENTERPRISE_HOST)
        }
    }
}
