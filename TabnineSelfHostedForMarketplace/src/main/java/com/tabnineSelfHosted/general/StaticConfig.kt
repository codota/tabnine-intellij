package com.tabnineSelfHosted.general

import com.intellij.openapi.util.IconLoader

object StaticConfig {
    const val TABNINE_ENTERPRISE_ID_RAW = "com.tabnine.TabNine-Enterprise"

    val ICON_AND_NAME_ENTERPRISE = IconLoader.findIcon("/icons/tabnine-enterprise-13px.png")

    val ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE =
        IconLoader.findIcon("/icons/tabnine-enterprise-connection-lost-13px.png")
}
