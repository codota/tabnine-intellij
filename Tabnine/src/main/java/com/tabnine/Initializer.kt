package com.tabnine

import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.tabnine.hover.HoverUpdater
import com.tabnine.lifecycle.BinaryInstantiatedActions
import com.tabnine.lifecycle.BinaryNotificationsLifecycle
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle
import com.tabnine.lifecycle.TabnineUpdater
import com.tabnine.lifecycle.UninstallReporter
import com.tabnine.statusBar.StatusBarUpdater
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.config.Config
import com.tabnineCommon.general.DependencyContainer.instanceOfBinaryRequestFacade
import com.tabnineCommon.general.DependencyContainer.instanceOfBinaryRun
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.lifecycle.BinaryStateService
import com.tabnineCommon.logging.initTabnineLogger
import com.tabnineCommon.notifications.ConnectionLostNotificationHandler
import com.tabnineCommon.selections.CompletionObserver
import java.util.concurrent.atomic.AtomicBoolean

class Initializer : PreloadingActivity(), StartupActivity {
    private var binaryNotificationsLifecycle: BinaryNotificationsLifecycle =
        BinaryNotificationsLifecycle(
            instanceOfBinaryRequestFacade(), BinaryInstantiatedActions(instanceOfBinaryRequestFacade())
        )
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
        PluginInstaller.addStateListener(UninstallListener(instanceOfBinaryRequestFacade(), UninstallReporter(instanceOfBinaryRun())))

        val statusBarUpdater = StatusBarUpdater(instanceOfBinaryRequestFacade())
        val hoverUpdater = HoverUpdater()
        CompletionObserver.subscribe {
            statusBarUpdater.updateStatusBar()
            hoverUpdater.update(it)
        }
    }

    companion object {
        private val connectionLostNotificationHandler = ConnectionLostNotificationHandler()
        private val initialized = AtomicBoolean(false)
    }
}
