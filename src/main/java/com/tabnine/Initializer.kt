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
import com.tabnine.dialogs.Dialogs.showRestartDialog
import com.tabnine.dialogs.TabnineEnterpriseUrlDialogWrapper
import com.tabnine.general.DependencyContainer
import com.tabnine.general.StaticConfig
import com.tabnine.general.Utils
import com.tabnine.lifecycle.BinaryNotificationsLifecycle
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle
import com.tabnine.lifecycle.BinaryStateService
import com.tabnine.lifecycle.TabnineUpdater
import com.tabnine.logging.initTabnineLogger
import com.tabnine.notifications.ConnectionLostNotificationHandler
import com.tabnine.userSettings.AppSettingsState.Companion.instance
import java.util.concurrent.atomic.AtomicBoolean

class Initializer : PreloadingActivity(), StartupActivity {
    private var binaryNotificationsLifecycle: BinaryNotificationsLifecycle? = null
    private var binaryPromotionStatusBarLifecycle: BinaryPromotionStatusBarLifecycle? = null
    override fun preload(indicator: ProgressIndicator) {
        initialize()
    }

    override fun runActivity(project: Project) {
        initialize()
    }

    private fun initialize() {
        val shouldInitialize = !(initialized.getAndSet(true) || ApplicationManager.getApplication().isUnitTestMode)
        if (shouldInitialize) {
            Logger.getInstance(javaClass)
                .info(
                    "Initializing for ${Config.CHANNEL}, plugin id = ${StaticConfig.TABNINE_PLUGIN_ID_RAW}"
                )
            connectionLostNotificationHandler.startConnectionLostListener()
            ServiceManager.getService(BinaryStateService::class.java).startUpdateLoop()
            initTabnineLogger()
            if (!Config.IS_ON_PREM) {
                binaryNotificationsLifecycle = DependencyContainer.instanceOfBinaryNotifications()
                binaryPromotionStatusBarLifecycle = DependencyContainer.instanceOfBinaryPromotionStatusBar()
                binaryNotificationsLifecycle?.poll()
                binaryPromotionStatusBarLifecycle?.poll()
                CapabilitiesService.getInstance().init()
                TabnineUpdater.pollUpdates()
                PluginInstaller.addStateListener(DependencyContainer.instanceOfUninstallListener())
            } else if (Config.IS_ON_PREM && !dialogShown.getAndSet(true)) {
                val cloud2Url = StaticConfig.getTabnineEnterpriseHost()
                if (cloud2Url.isPresent) {
                    Logger.getInstance(javaClass)
                        .info(String.format("Tabnine Enterprise host is configured: %s", cloud2Url.get()))
                    Utils.setCustomRepository(cloud2Url.get())
                } else {
                    Logger.getInstance(javaClass)
                        .warn(
                            "Tabnine Enterprise host is not configured, showing some nice dialog"
                        )
                    ApplicationManager.getApplication().invokeLater(
                        Runnable {
                            val dialog = TabnineEnterpriseUrlDialogWrapper(null)
                            if (dialog.showAndGet()) {
                                val url = dialog.inputData
                                instance.cloud2Url = url
                                Utils.setCustomRepository(url)
                                showRestartDialog("Self hosted URL configured successfully - Restart your IDE for the change to take effect.")
                            }
                        }
                    )
                }
            }
        }
    }

    companion object {
        private val connectionLostNotificationHandler = ConnectionLostNotificationHandler()
        private val dialogShown = AtomicBoolean(false)
        private val initialized = AtomicBoolean(false)
    }
}
