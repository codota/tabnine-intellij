package com.tabnine.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.tabnine.binary.requests.config.StateResponse
import com.tabnine.general.StaticConfig
import com.tabnine.general.Utils.getHoursDiff
import com.tabnine.lifecycle.BinaryStateChangeNotifier
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

private const val INTERVAL_BETWEEN_NOTIFICATIONS_HOURS = 5
private const val NOTIFICATION_CONTENT =
    "<b>Tabnine lost internet connection.</b>" +
        "<br/>" +
        "If your internet is connected and you're seeing this message, contact Tabnine support."

class ConnectionLostNotificationHandler {
    private val ourGroup = NotificationGroup(StaticConfig.BRAND_NAME, NotificationDisplayType.STICKY_BALLOON, false)
    private var lastNotificationTime: Date? = null
    private var isRegistered = AtomicBoolean(false)

    fun handleConnectionLostEvent() {
        if (!isRegistered.getAndSet(true)) {
            ApplicationManager.getApplication().messageBus.connect()
                .subscribe(
                    BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
                    BinaryStateChangeNotifier { stateResponse ->
                        if (stateResponse.isConnectionHealthy == false && shouldShowNotification(stateResponse)) {
                            lastNotificationTime = Date()
                            showNotification()
                        }
                    }
                )
        }
    }

    private fun showNotification() {
        val notification = Notification(ourGroup.displayId, StaticConfig.CONNECTION_LOST_NOTIFICATION_ICON, NotificationType.INFORMATION)
        notification
            .setContent(NOTIFICATION_CONTENT)
            .addAction(object : AnAction("Dismiss") {
                override fun actionPerformed(e: AnActionEvent) {
                    notification.expire()
                }
            })
        Notifications.Bus.notify(notification)
    }

    private fun shouldShowNotification(stateResponse: StateResponse): Boolean {
        return stateResponse.isConnectionHealthy == false &&
            (lastNotificationTime == null || getHoursDiff(lastNotificationTime, Date()) > INTERVAL_BETWEEN_NOTIFICATIONS_HOURS)
    }
}
