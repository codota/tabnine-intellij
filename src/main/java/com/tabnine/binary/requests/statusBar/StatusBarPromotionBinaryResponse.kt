package com.tabnine.binary.requests.statusBar

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tabnine.binary.BinaryResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class StatusBarPromotionBinaryResponse(
        var id: String?,
        var message: String?,
) : BinaryResponse
