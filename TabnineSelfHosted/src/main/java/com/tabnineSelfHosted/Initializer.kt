package com.tabnineSelfHosted

import com.intellij.ide.plugins.PluginManagerCore.isUnitTestMode
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.tabnineCommon.lifecycle.BinaryStateService
import com.tabnineCommon.logging.initTabnineLogger
import com.tabnineCommon.notifications.ConnectionLostNotificationHandler
import com.tabnineCommon.userSettings.AppSettingsState
import com.tabnineSelfHosted.general.StaticConfig
import java.util.Optional
import java.util.concurrent.atomic.AtomicBoolean

class Initializer : PreloadingActivity(), StartupActivity {
    override fun preload(indicator: ProgressIndicator) {
        initialize()
    }

    override fun runActivity(project: Project) {
        initialize()
    }

    private fun initialize() {
        if (initialized.getAndSet(true) || isUnitTestMode) {
            return
        }
        initTabnineLogger()
        connectionLostNotificationHandler.startConnectionLostListener()
        SelfHostedInitializer().initialize(AppSettingsState.instance.cloud2Url)
        SelfHostedProviderOfThings.INSTANCE.serverUrl = Optional.ofNullable(StaticConfig.getBundleUpdateUrl())
        service<BinaryStateService>().startUpdateLoop()
    }

    companion object {
        private val connectionLostNotificationHandler = ConnectionLostNotificationHandler(StaticConfig.LOST_CONNECTION_NOTIFICATION_CONTENT)
        private val initialized = AtomicBoolean(false)
    }
}
