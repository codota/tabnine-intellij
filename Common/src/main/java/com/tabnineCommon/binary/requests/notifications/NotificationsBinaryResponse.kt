package com.tabnineCommon.binary.requests.notifications

import com.tabnineCommon.binary.BinaryResponse

data class NotificationsBinaryResponse(var notifications: List<BinaryNotification>? = null) :
    BinaryResponse
