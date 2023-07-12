package com.tabnineCommon.UIMessages

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.tabnineCommon.general.StaticConfig

object Notifications {
    fun showInfoNotification(title: String, message: String, actions: List<AnAction>) {
        val notification = Notification(
            StaticConfig.TABNINE_NOTIFICATION_GROUP.displayId,
            StaticConfig.NOTIFICATION_ICON,
            title,
            null,
            message,
            NotificationType.INFORMATION,
            null
        )

        actions.forEach { notification.addAction(it) }

        notification.notify(null)
    }
}
