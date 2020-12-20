package com.tabnine.binary.requests.notifications

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

enum class NotificationActions {
    @JsonEnumDefaultValue
    None,
    OpenHub,
    OpenLp,
    OpenBuy,
}