package com.tabnine

import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.tabnine.capabilities.CapabilitiesService
import com.tabnine.config.Config
import com.tabnine.general.DependencyContainer
import com.tabnine.general.DependencyContainer.instanceOfBinaryRequestFacade
import com.tabnine.general.StaticConfig
import com.tabnine.lifecycle.BinaryNotificationsLifecycle
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle
import com.tabnine.lifecycle.BinaryStateService
import com.tabnine.lifecycle.TabnineUpdater
import com.tabnine.logging.initTabnineLogger
import com.tabnine.notifications.ConnectionLostNotificationHandler
import com.tabnine.selections.CompletionObserver
import com.tabnine.statusBar.StatusBarUpdater
import java.util.concurrent.atomic.AtomicBoolean

class Initializer : PreloadingActivity(), StartupActivity {
    private var binaryNotificationsLifecycle: BinaryNotificationsLifecycle =
        DependencyContainer.instanceOfBinaryNotifications()
    private var binaryPromotionStatusBarLifecycle = BinaryPromotionStatusBarLifecycle(
        StatusBarUpdater(instanceOfBinaryRequestFacade())
    )
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

        val statusBarUpdater = StatusBarUpdater(instanceOfBinaryRequestFacade())
        CompletionObserver.subscribe {
            statusBarUpdater.updateStatusBar()
        }
    }

    companion object {
        private val connectionLostNotificationHandler = ConnectionLostNotificationHandler()
        private val initialized = AtomicBoolean(false)
    }
}
