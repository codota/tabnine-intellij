package com.tabnineSelfHosted

import com.intellij.notification.NotificationType
import com.tabnineCommon.general.StaticConfig.TABNINE_TIMED_NOTIFICATION_GROUP

fun showUserLoggedInNotification() {
    TABNINE_TIMED_NOTIFICATION_GROUP.createNotification(
        "Congratulations! Tabnine is up and running.",
        NotificationType.INFORMATION
    ).notify(null)
}
