package com.tabnineSelfHosted.general

import com.intellij.openapi.diagnostic.Logger
import com.tabnineSelfHosted.userSettings.AppSettingsState.Companion.instance

object StaticConfig {
    const val TABNINE_ENTERPRISE_ID_RAW = "com.tabnine.TabNine-Enterprise"
    const val LOST_CONNECTION_NOTIFICATION_CONTENT =
        "<b>Tabnine server connectivity issue.</b><br/>Please check your network setup and access to your configured Tabnine Enterprise Host"

    @JvmStatic
    fun getBundleUpdateUrl(): String? {
        val tabnineEnterpriseHost = getTabnineEnterpriseHost()
        if (tabnineEnterpriseHost == null) {
            Logger.getInstance(StaticConfig::class.java).warn("On prem version but server url not set")
            return null
        }
        return listOf(tabnineEnterpriseHost, "update", "bundles").joinToString("/")
    }

    @JvmStatic
    fun getTabnineEnterpriseHost(): String? {
        val path = instance.cloud2Url
        return path.ifBlank { null }
    }
}
