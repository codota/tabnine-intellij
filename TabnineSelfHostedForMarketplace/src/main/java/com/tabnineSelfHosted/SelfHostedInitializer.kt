package com.tabnineSelfHosted

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.tabnineSelfHosted.dialogs.TabnineEnterpriseUrlDialogWrapper
import com.tabnineSelfHosted.general.StaticConfig
import com.tabnineSelfHosted.userSettings.AppSettingsState
import java.util.concurrent.atomic.AtomicBoolean

class SelfHostedInitializer : StartupActivity {
    override fun runActivity(project: Project) {
        val host = AppSettingsState.instance.cloud2Url
        initialize(host)
    }

    fun initialize(host: String) {
        if (initialized.getAndSet(true) || ApplicationManager.getApplication().isUnitTestMode) {
            return
        }

        Logger.getInstance(javaClass)
            .info(
                "Initializing for self-hosted, plugin id = ${StaticConfig.TABNINE_ENTERPRISE_ID_RAW}"
            )

        requireSelfHostedUrl(host)
    }

    private fun requireSelfHostedUrl(host: String) {
        if (host.isNotBlank()) {
            Logger.getInstance(javaClass)
                .info(String.format("Tabnine Enterprise host is configured: %s", host))
            // This is for users that already configured the cloud url, but didn't set the repository.
            // Duplication is handle inside
            Utils.setCustomRepository(host)
            TabnineEnterprisePluginInstaller().installTabnineEnterprisePlugin(host)
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
                        TabnineEnterprisePluginInstaller().installTabnineEnterprisePlugin(null)
                    }
                }
            )
        }
    }

    companion object {
        private val initialized = AtomicBoolean(false)
    }
}
