package com.tabnine.binary.requests.statusBar

import com.tabnine.binary.BinaryResponse

data class StatusBarPromotionBinaryResponse(
        var id: String?,
        var message: String?,
        var options: List<PromotionOptions>,
) : BinaryResponse

data class PromotionOptions(
        var key: String?,
        var action: StatusBarActionActions?,
)

enum class StatusBarActionActions {
    None
}
