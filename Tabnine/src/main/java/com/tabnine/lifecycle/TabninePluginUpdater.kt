package com.tabnine.lifecycle

import com.intellij.ide.plugins.PluginManagerConfigurable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.updateSettings.impl.PluginDownloader
import com.intellij.openapi.updateSettings.impl.UpdateChecker.getPluginUpdates
import com.intellij.util.Alarm
import com.tabnineCommon.UIMessages.Notifications
import com.tabnineCommon.general.StaticConfig.TABNINE_PLUGIN_ID
import com.tabnineCommon.userSettings.AppSettingsConfigurable
import com.tabnineCommon.userSettings.AppSettingsState
import java.util.concurrent.atomic.AtomicBoolean

private const val ONE_DAY_IN_MILLIS = 24L * 60 * 60 * 1000
private const val TEN_SECONDS_IN_MILLIS = 10L * 1000
private const val RETRIES = 3L

class TabninePluginUpdater {
    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD)

    private fun enqueueUpdateCheckJob(delayMillis: Long, retries: Long) {
        alarm.addRequest(
            Runnable {
                checkForUpdates()
                enqueueUpdateCheckJob(ONE_DAY_IN_MILLIS, retries)
            },
            delayMillis
        )
    }

    private fun checkForUpdates() {
        ApplicationManager.getApplication()
            .executeOnPooledThread {
                try {
                    val application = ApplicationManager.getApplication() ?: return@executeOnPooledThread
                    if (application.isDisposed) return@executeOnPooledThread
                    val ourDownloader =
                        getPluginUpdates()?.find { it.id == TABNINE_PLUGIN_ID } ?: return@executeOnPooledThread

                    if (AppSettingsState.instance.autoPluginUpdates) {
                        installUpdate(ourDownloader)
                    } else {
                        showAvailableUpdateNotification(ourDownloader)
                    }
                } catch (e: Exception) {
                    Logger.getInstance(javaClass).warn("Error checking for plugin updates", e)
                }
            }
    }

    private fun installUpdate(ourDownloader: PluginDownloader) {
        try {
            val successfullyPrepared = ourDownloader.prepareToInstall(EmptyProgressIndicator())
            if (successfullyPrepared) {
                ourDownloader.install()
                showSuccessfulInstallationNotification()
            }
        } catch (e: Exception) {
            Logger.getInstance(javaClass).warn("Error installing plugin", e)
        }
    }

    private fun showSuccessfulInstallationNotification() {
        Notifications.showInfoNotification(
            "Tabnine plugin successfully updated",
            "Restart your IDE to apply the update.",
            listOf(
                object : AnAction("Restart") {
                    override fun actionPerformed(e: AnActionEvent) {
                        (ApplicationManager.getApplication() as ApplicationEx).restart(false)
                    }
                },
                manageAutoUpdatesAction()
            )
        )
    }

    private fun showAvailableUpdateNotification(ourDownloader: PluginDownloader) {
        Notifications.showInfoNotification(
            "Tabnine plugin update available",
            "It is recommended to update Tabnine plugin.",
            listOf(
                object : AnAction("Update") {
                    override fun actionPerformed(e: AnActionEvent) {
                        PluginManagerConfigurable.showPluginConfigurable(e.project, listOf(ourDownloader))
                    }
                },
                manageAutoUpdatesAction()
            )
        )
    }

    private fun manageAutoUpdatesAction() = object : AnAction("Manage Tabnine Auto Updates") {
        override fun actionPerformed(e: AnActionEvent) {
            ShowSettingsUtil.getInstance().showSettingsDialog(e.project, AppSettingsConfigurable::class.java)
        }
    }

    companion object {
        private val instance = TabninePluginUpdater()
        private val initialized = AtomicBoolean(false)
        fun start() {
            if (initialized.compareAndSet(false, true)) {
                instance.enqueueUpdateCheckJob(TEN_SECONDS_IN_MILLIS, RETRIES)
            }
        }
    }
}
