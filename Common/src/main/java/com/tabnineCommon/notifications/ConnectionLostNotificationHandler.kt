package com.tabnineCommon.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.config.Config
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.general.Utils.getHoursDiff
import com.tabnineCommon.lifecycle.BinaryStateSingleton
import com.tabnineCommon.state.CompletionsState
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

private const val INTERVAL_BETWEEN_NOTIFICATIONS_HOURS = 5
private const val NOTIFICATION_CONTENT =
    "<b>Tabnine lost internet connection.</b>" +
        "<br/>" +
        "If your internet is connected and you're seeing this message, contact Tabnine support."

private const val SELF_HOSTED_NOTIFICATION_CONTENT =
    "<b>Tabnine server connectivity issue.</b>" +
        "<br/>" +
        "Please check your network setup and access to your configured Tabnine Enterprise Host"

class ConnectionLostNotificationHandler {
    private var lastNotificationTime: Date? = null
    private var isRegistered = AtomicBoolean(false)

    fun startConnectionLostListener() {
        if (isRegistered.getAndSet(true)) {
            return
        }
        BinaryStateSingleton.instance.onChange(
            BinaryStateSingleton.OnChange { stateResponse ->
                if (shouldShowNotification(stateResponse)) {
                    lastNotificationTime = Date()
                    showNotification()
                }
            }
        )
    }

    private fun showNotification() {
        val notification = Notification(StaticConfig.TABNINE_NOTIFICATION_GROUP.displayId, StaticConfig.getConnectionLostNotificationIcon(), NotificationType.INFORMATION)
        notification
            .setContent(getNotificationContent())
            .addAction(object : AnAction("Dismiss") {
                override fun actionPerformed(e: AnActionEvent) {
                    notification.expire()
                }
            })
        Notifications.Bus.notify(notification)
    }

    private fun getNotificationContent() = if (Config.IS_SELF_HOSTED) {
        SELF_HOSTED_NOTIFICATION_CONTENT
    } else {
        NOTIFICATION_CONTENT
    }

    private fun shouldShowNotification(stateResponse: StateResponse): Boolean {
        return CompletionsState.isCompletionsEnabled() && stateResponse.cloudConnectionHealthStatus == CloudConnectionHealthStatus.Failed &&
            (lastNotificationTime == null || getHoursDiff(lastNotificationTime, Date()) > INTERVAL_BETWEEN_NOTIFICATIONS_HOURS)
    }
}
