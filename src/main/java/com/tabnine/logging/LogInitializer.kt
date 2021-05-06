package com.tabnine.logging

import com.intellij.openapi.application.ApplicationInfo
import com.tabnine.config.Config
import com.tabnine.general.Utils
import io.sentry.Scope
import io.sentry.Sentry
import org.apache.log4j.LogManager

fun init() {
    Sentry.init()
    Sentry.configureScope { scope: Scope ->
        scope.setTag("ide", ApplicationInfo.getInstance().versionName)
        scope.setTag("ideVersion", ApplicationInfo.getInstance().fullVersion)
        scope.setTag("pluginVersion", Utils.cmdSanitize(Utils.getTabNinePluginVersion()))
        scope.setTag("os", System.getProperty("os.name"))
        scope.setTag("channel", Config.CHANNEL)
    }
    val tabnineLogger = LogManager.getLogger("#com.tabnine")
    val sentryAppender = SentryAppender()
    tabnineLogger.addAppender(sentryAppender)
}
