package com.tabnineSelfHosted.binary.requests.userInfo

import com.tabnineCommon.binary.BinaryRequest

class UserInfoRequest : BinaryRequest<UserInfoResponse> {
    override fun response(): Class<UserInfoResponse> {
        return UserInfoResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("UserInfo" to this)
    }
}
