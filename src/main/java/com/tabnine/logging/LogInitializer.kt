package com.tabnine.logging

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.PermanentInstallationID
import com.tabnine.config.Config
import com.tabnine.general.Utils
import io.sentry.Scope
import io.sentry.Sentry
import io.sentry.protocol.User
import org.apache.log4j.LogManager

fun init() {
    Sentry.init()
    if (!Sentry.isEnabled()) {
        return
    }
    Sentry.configureScope { scope: Scope ->
        scope.setTag("ide", ApplicationInfo.getInstance().versionName)
        scope.setTag("ideVersion", ApplicationInfo.getInstance().fullVersion)
        scope.setTag("pluginVersion", Utils.cmdSanitize(Utils.getTabNinePluginVersion()))
        scope.setTag("os", System.getProperty("os.name"))
        scope.setTag("channel", Config.CHANNEL)
    }
    val user = User().apply {
        id = PermanentInstallationID.get()
    }
    Sentry.setUser(user)
    val tabnineLogger = LogManager.getLogger("#com.tabnine")
    val sentryAppender = SentryAppender()
    tabnineLogger.addAppender(sentryAppender)
}
