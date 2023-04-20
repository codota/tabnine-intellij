package com.tabnineSelfHosted

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.tabnineCommon.config.Config
import com.tabnineCommon.dialogs.Dialogs.showRestartDialog
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.general.Utils
import com.tabnineCommon.lifecycle.BinaryStateService
import com.tabnineCommon.logging.initTabnineLogger
import com.tabnineCommon.notifications.ConnectionLostNotificationHandler
import com.tabnineCommon.userSettings.AppSettingsState
import com.tabnineSelfHosted.dialogs.TabnineEnterpriseUrlDialogWrapper
import java.util.concurrent.atomic.AtomicBoolean

class SelfHostedInitializer : PreloadingActivity(), StartupActivity {
    override fun preload(indicator: ProgressIndicator) {
        initialize()
    }

    override fun runActivity(project: Project) {
        initialize()
    }

    fun initialize() {
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
        requireSelfHostedUrl()
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
