package com.tabnineCommon.chat.commandHandlers.utils

import com.tabnineCommon.config.Config
import com.tabnineCommon.general.StaticConfig

fun getServerUrl(): String? {
    if (!Config.IS_SELF_HOSTED) return null
    return StaticConfig.getTabnineEnterpriseHost().orElse(null)
}
