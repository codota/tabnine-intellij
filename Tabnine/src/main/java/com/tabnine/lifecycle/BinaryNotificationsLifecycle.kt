package com.tabnine.lifecycle

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnine.binary.requests.notifications.NotificationOptions
import com.tabnine.binary.requests.notifications.NotificationsBinaryRequest
import com.tabnine.binary.requests.notifications.actions.NotificationActionRequest
import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.notifications.shown.NotificationShownRequest
import com.tabnineCommon.general.StaticConfig.* // ktlint-disable no-wildcard-imports
import java.util.Timer
import java.util.TimerTask

class BinaryNotificationsLifecycle(
    private val binaryRequestFacade: BinaryRequestFacade,
    private val actionVisitor: BinaryInstantiatedActions
) {
    fun poll() {
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    binaryRequestFacade.executeRequest(NotificationsBinaryRequest())?.notifications?.forEach { binaryNotification ->
                        if (PropertiesComponent.getInstance().getBoolean(storageKey(binaryNotification.id), false)) {
                            return
                        }

                        val notification = Notification(TABNINE_NOTIFICATION_GROUP.displayId, NOTIFICATION_ICON, NotificationType.INFORMATION)

                        notification.setContent(binaryNotification.message)

                        binaryNotification.options?.stream()?.forEach { option: NotificationOptions ->
                            notification.addAction(object : AnAction(option.key) {
                                override fun actionPerformed(e: AnActionEvent) {
                                    binaryRequestFacade.executeRequest(
                                        NotificationActionRequest(
                                            binaryNotification.id,
                                            option.key,
                                            binaryNotification.message,
                                            binaryNotification.notificationType,
                                            option.actions,
                                            binaryNotification.state,
                                        )
                                    )
                                    if (option.actions?.any { it == OPEN_HUB_ACTION } == true) {
                                        actionVisitor.openHub()
                                    }
                                    notification.expire()
                                }
                            })
                        }

                        Notifications.Bus.notify(notification)
                        binaryRequestFacade.executeRequest(
                            NotificationShownRequest(
                                binaryNotification.id,
                                binaryNotification.message, binaryNotification.notificationType, binaryNotification.state
                            )
                        )
                        PropertiesComponent.getInstance().setValue(storageKey(binaryNotification.id), true)
                    }
                }
            },
            BINARY_NOTIFICATION_POLLING_INTERVAL, BINARY_NOTIFICATION_POLLING_INTERVAL
        )
    }

    private fun storageKey(id: String?) = "tabnine-notifications-shown-$id"
}
