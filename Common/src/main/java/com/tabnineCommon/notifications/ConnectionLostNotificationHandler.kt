package com.tabnineCommon.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.general.Utils.getHoursDiff
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

private const val INTERVAL_BETWEEN_NOTIFICATIONS_HOURS = 5

class ConnectionLostNotificationHandler(private val notificationMessage: String) {
    private var lastNotificationTime: Date? = null
    private var isRegistered = AtomicBoolean(false)

    fun startConnectionLostListener() {
        if (isRegistered.getAndSet(true)) {
            return
        }
        ServiceManager.messageBus.connect()
            .subscribe(
                BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
                BinaryStateChangeNotifier { stateResponse ->
                    if (shouldShowNotification(stateResponse)) {
                        lastNotificationTime = Date()
                        showNotification()
                    }
                }
            )
    }

    private fun showNotification() {
        val notification = Notification(StaticConfig.TABNINE_NOTIFICATION_GROUP.displayId, StaticConfig.CONNECTION_LOST_NOTIFICATION_ICON, NotificationType.INFORMATION)
        notification
            .setContent(notificationMessage)
            .addAction(object : AnAction("Dismiss") {
                override fun actionPerformed(e: AnActionEvent) {
                    notification.expire()
                }
            })
        Notifications.Bus.notify(notification)
    }

    private fun shouldShowNotification(stateResponse: StateResponse): Boolean {
        return stateResponse.cloudConnectionHealthStatus == CloudConnectionHealthStatus.Failed &&
            (lastNotificationTime == null || getHoursDiff(lastNotificationTime, Date()) > INTERVAL_BETWEEN_NOTIFICATIONS_HOURS)
    }
}
