package com.tabnine.lifecycle

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.notifications.NotificationOptions
import com.tabnine.binary.requests.notifications.NotificationsBinaryRequest
import com.tabnine.binary.requests.notifications.actions.NotificationActionRequest
import com.tabnine.binary.requests.notifications.shown.NotificationShownRequest
import com.tabnine.general.StaticConfig.*
import java.util.*

class BinaryNotificationsLifecycle(
    private val binaryRequestFacade: BinaryRequestFacade,
    private val actionVisitor: GlobalActionVisitor
) {
    fun poll() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                binaryRequestFacade.executeRequest(NotificationsBinaryRequest())?.notifications?.forEach { binaryNotification ->
                    if (PropertiesComponent.getInstance().getBoolean(storageKey(binaryNotification.id), false)) {
                        return
                    }

                    val notification = Notification(BRAND_NAME, NOTIFICATION_ICON, NotificationType.INFORMATION)

                    notification.setContent(binaryNotification.message)

                    binaryNotification.options?.stream()?.findFirst()?.ifPresent { o: NotificationOptions ->
                        notification.addAction(object : AnAction(o.key) {
                            override fun actionPerformed(e: AnActionEvent) {
                                binaryRequestFacade.executeRequest(
                                    NotificationActionRequest(
                                        binaryNotification.id,
                                        o.key,
                                        binaryNotification.message,
                                        binaryNotification.notificationType,
                                        o.action
                                    )
                                )
                                o.action?.visit(actionVisitor)
                                notification.expire()
                            }
                        })
                    }

                    Notifications.Bus.notify(notification)
                    binaryRequestFacade.executeRequest(NotificationShownRequest(binaryNotification.message))
                    PropertiesComponent.getInstance().setValue(storageKey(binaryNotification.id), true)
                }
            }
        }, BINARY_NOTIFICATION_POLLING_INTERVAL, BINARY_NOTIFICATION_POLLING_INTERVAL)
    }

    private fun storageKey(id: String?) = "tabnine-notifications-shown-${id}"
}