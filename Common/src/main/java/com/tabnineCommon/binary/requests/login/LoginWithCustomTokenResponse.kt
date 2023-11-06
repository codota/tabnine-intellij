package com.tabnineCommon.binary.requests.login

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryResponse

data class LoginWithCustomTokenResponse(
    @SerializedName("is_success")
    var isSuccess: Boolean? = null,
) : BinaryResponse
