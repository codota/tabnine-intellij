package com.tabnineSelfHosted

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.ServiceManager
import com.tabnineCommon.general.StaticConfig.TABNINE_TIMED_NOTIFICATION_GROUP
import com.tabnineSelfHosted.binary.lifecycle.UserInfoService

fun showUserLoggedInNotification() {
    val userInfo = ServiceManager.getService(UserInfoService::class.java).fetchAndGet()
    // show notification only if the user is part of a team
    if (userInfo?.team == null) {
        return
    }

    TABNINE_TIMED_NOTIFICATION_GROUP.createNotification(
        "Congratulations! Tabnine is up and running.",
        NotificationType.INFORMATION
    ).notify(null)
}
