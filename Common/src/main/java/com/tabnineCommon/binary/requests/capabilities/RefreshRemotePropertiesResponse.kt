package com.tabnineCommon.binary.requests.capabilities

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryResponse

data class RefreshRemotePropertiesResponse(
    @SerializedName("type")
    var type: String? = null,
) : BinaryResponse

enum class RefreshRemotePropertiesResponseType {
    @SerializedName("Success")
    Success,

    @SerializedName("Error")
    Error,
}
