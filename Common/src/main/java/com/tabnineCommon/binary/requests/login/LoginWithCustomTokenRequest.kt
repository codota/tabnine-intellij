package com.tabnineCommon.binary.requests.login

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryRequest
import java.util.Collections.singletonMap

data class LoginWithCustomTokenRequest(private val onSuccessFn: (() -> Unit)? = null) : BinaryRequest<LoginWithCustomTokenResponse> {

    @SerializedName(value = "custom_token")
    var customToken: String? = null

    override fun response(): Class<LoginWithCustomTokenResponse> {
        return LoginWithCustomTokenResponse::class.java
    }

    override fun serialize(): Any {
        return singletonMap("LoginWithCustomToken", this)
    }
}
