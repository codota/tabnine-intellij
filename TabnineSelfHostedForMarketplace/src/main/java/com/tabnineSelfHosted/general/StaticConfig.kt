package com.tabnineSelfHosted.general

import com.intellij.openapi.util.IconLoader

object StaticConfig {
    const val TABNINE_ENTERPRISE_ID_RAW = "com.tabnine.TabNine-Enterprise"

    @JvmField
    val ICON_AND_NAME_ENTERPRISE = IconLoader.getIcon("/icons/tabnine-enterprise-13px.png", javaClass)

    @JvmField
    val ICON_AND_NAME_NO_URL =
        IconLoader.getIcon("/icons/tabnine-enterprise-no-url-13px.png", javaClass)

    @JvmField
    val ICON_AND_NAME_CONNECTIVITY_ISSUES = IconLoader.getIcon("/icons/tabnine-enterprise-connectivity-issues-13px.png", javaClass)

    @JvmField
    val ICON_AND_NAME_NOT_LOGGED_IN_ENTERPRISE = IconLoader.getIcon("icons/tabnine-enterprise-sign-in-13px.png", javaClass)

    @JvmField
    val ICON_AND_NAME_NOT_IN_TEAM = IconLoader.getIcon("icons/tabnine-enterprise-not-in-team-13px.png", javaClass)
}
