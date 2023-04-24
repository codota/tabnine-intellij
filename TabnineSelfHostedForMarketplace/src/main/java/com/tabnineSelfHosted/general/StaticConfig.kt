package com.tabnineSelfHosted.general

object StaticConfig {
    private const val TABNINE_ENTERPRISE_HOST = "TABNINE_ENTERPRISE_HOST"
    const val TABNINE_ENTERPRISE_ID_RAW = "com.tabnine.TabNine-Enterprise"

    @JvmStatic
    fun getTabnineEnterpriseHost(host: String): String? {
        return host.ifEmpty {
            System.getProperty(TABNINE_ENTERPRISE_HOST)
        }
    }
}
