package com.tabnine

import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.tabnine.general.DependencyContainer
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.config.Config
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.lifecycle.BinaryNotificationsLifecycle
import com.tabnineCommon.lifecycle.BinaryStateService
import com.tabnineCommon.lifecycle.TabnineUpdater
import com.tabnineCommon.logging.initTabnineLogger
import com.tabnineCommon.notifications.ConnectionLostNotificationHandler
import java.util.concurrent.atomic.AtomicBoolean

class Initializer : PreloadingActivity(), StartupActivity {
    private var binaryNotificationsLifecycle: BinaryNotificationsLifecycle =
        DependencyContainer.instanceOfBinaryNotifications()
    private var binaryPromotionStatusBarLifecycle = DependencyContainer.instanceOfBinaryPromotionStatusBar()
    override fun preload(indicator: ProgressIndicator) {
        initialize()
    }

    override fun runActivity(project: Project) {
        initialize()
    }

    private fun initialize() {
        val shouldInitialize = !(initialized.getAndSet(true) || ApplicationManager.getApplication().isUnitTestMode)
        if (!shouldInitialize) {
            return
        }

        Logger.getInstance(javaClass)
            .info(
                "Initializing for ${Config.CHANNEL}, plugin id = ${StaticConfig.TABNINE_PLUGIN_ID_RAW}"
            )
        connectionLostNotificationHandler.startConnectionLostListener()
        ServiceManager.getService(BinaryStateService::class.java).startUpdateLoop()
        initTabnineLogger()

        initListeners()
    }

    private fun initListeners() {
        binaryNotificationsLifecycle.poll()
        binaryPromotionStatusBarLifecycle?.poll()
        CapabilitiesService.getInstance().init()
        TabnineUpdater.pollUpdates()
        PluginInstaller.addStateListener(DependencyContainer.instanceOfUninstallListener())
    }

    companion object {
        private val connectionLostNotificationHandler = ConnectionLostNotificationHandler(StaticConfig.LOST_CONNECTION_NOTIFICATION_CONTENT)
        private val initialized = AtomicBoolean(false)
    }
}
