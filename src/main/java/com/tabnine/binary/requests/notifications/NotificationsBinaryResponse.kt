package com.tabnine.binary.requests.notifications

import com.tabnine.binary.BinaryResponse

data class NotificationsBinaryResponse(var notifications: List<BinaryNotification>? = null) : BinaryResponse