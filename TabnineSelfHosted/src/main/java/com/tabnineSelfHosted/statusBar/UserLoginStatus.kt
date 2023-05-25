package com.tabnineSelfHosted.statusBar

import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus

enum class UserLoginStatus {
    Unknown,
    LoggedIn,
    LoggedOut,
}

fun getUserLoginStatus(connectionStatus: CloudConnectionHealthStatus?, userEmail: String?): UserLoginStatus {
    if (connectionStatus == CloudConnectionHealthStatus.Failed) {
        return UserLoginStatus.Unknown
    }

    if (userEmail.isNullOrBlank()) {
        return UserLoginStatus.LoggedOut
    }

    return UserLoginStatus.LoggedIn
}
