package com.tabnine-enterprise

import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.tabnine.capabilities.CapabilitiesService
import com.tabnine-common.config.Config
import com.tabnine-common.dialogs.Dialogs.showRestartDialog
import com.tabnine-common.dialogs.TabnineEnterpriseUrlDialogWrapper
import com.tabnine-common.general.DependencyContainer
import com.tabnine-common.general.StaticConfig
import com.tabnine-common.general.Utils
import com.tabnine-common.lifecycle.BinaryNotificationsLifecycle
import com.tabnine-common.lifecycle.BinaryStateService
import com.tabnine-common.lifecycle.TabnineEnterprisePluginInstaller
import com.tabnine-common.lifecycle.TabnineUpdater
import com.tabnine-common.logging.initTabnineLogger
import com.tabnine-common.notifications.ConnectionLostNotificationHandler
import com.tabnine-common.userSettings.AppSettingsState
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
        if (Config.IS_SELF_HOSTED) {
            requireSelfHostedUrl()
        } else {
            initListeners()
        }
    }

    private fun initListeners() {
        binaryNotificationsLifecycle.poll()
        binaryPromotionStatusBarLifecycle?.poll()
        CapabilitiesService.getInstance().init()
        TabnineUpdater.pollUpdates()
        PluginInstaller.addStateListener(DependencyContainer.instanceOfUninstallListener())
    }

    private fun requireSelfHostedUrl() {
        val cloud2Url = StaticConfig.getTabnineEnterpriseHost()
        if (cloud2Url.isPresent) {
            Logger.getInstance(javaClass)
                .info(String.format("Tabnine Enterprise host is configured: %s", cloud2Url.get()))
            // This is for users that already configured the cloud url, but didn't set the repository.
            // Duplication is handle inside
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
                        AppSettingsState.instance.cloud2Url = url
                        showRestartDialog("Self hosted URL configured successfully - Restart your IDE for the change to take effect.")
                    }
                }
            )
        }
        TabnineEnterprisePluginInstaller().installTabnineEnterprisePlugin()
    }

    companion object {
        private val connectionLostNotificationHandler = ConnectionLostNotificationHandler()
        private val initialized = AtomicBoolean(false)
    }
}
