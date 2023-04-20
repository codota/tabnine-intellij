package com.tabnineSelfHosted

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.tabnineSelfHosted.dialogs.TabnineEnterpriseUrlDialogWrapper
import com.tabnineSelfHosted.general.StaticConfig
import com.tabnineSelfHosted.userSettings.AppSettingsState
import java.util.concurrent.atomic.AtomicBoolean

class SelfHostedInitializer : PreloadingActivity(), StartupActivity {
    override fun preload(indicator: ProgressIndicator) {
        initialize()
    }

    override fun runActivity(project: Project) {
        initialize()
    }

    fun initialize() {
        if (initialized.getAndSet(true) || ApplicationManager.getApplication().isUnitTestMode) {
            return
        }

        Logger.getInstance(javaClass)
            .info(
                "Initializing for self-hosted, plugin id = ${StaticConfig.TABNINE_ENTERPRISE_ID_RAW}"
            )

        requireSelfHostedUrl()
    }

    private fun requireSelfHostedUrl() {
        val cloud2Url = StaticConfig.getTabnineEnterpriseHost()
        if (cloud2Url != null) {
            Logger.getInstance(javaClass)
                .info(String.format("Tabnine Enterprise host is configured: %s", cloud2Url))
            // This is for users that already configured the cloud url, but didn't set the repository.
            // Duplication is handle inside
            Utils.setCustomRepository(cloud2Url)
            TabnineEnterprisePluginInstaller().installTabnineEnterprisePlugin()
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
                        TabnineEnterprisePluginInstaller().installTabnineEnterprisePlugin()
                    }
                }
            )
        }
    }

    companion object {
        private val initialized = AtomicBoolean(false)
    }
}
