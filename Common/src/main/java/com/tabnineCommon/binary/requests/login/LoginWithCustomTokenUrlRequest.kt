package com.tabnineCommon.binary.requests.login

import com.tabnineCommon.binary.BinaryRequest
import java.util.Collections.singletonMap

data class LoginWithCustomTokenUrlRequest(private val onSuccessFn: (() -> Unit)? = null) : BinaryRequest<String> {
    override fun response(): Class<String> {
        return String::class.java
    }

    override fun serialize(): Any {
        return singletonMap("LoginWithCustomTokenUrl", this)
    }
}
