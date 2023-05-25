package com.tabnineSelfHosted.binary.requests.userInfo

import com.tabnineCommon.binary.BinaryResponse

data class UserInfoResponse(
    var email: String = "",
    var team: Team? = null,
    var isLoggedIn: Boolean = false,
    var verified: Boolean = false,
) : BinaryResponse

data class Team(
    val name: String = "",
    val role: Role = Role.Member
)

enum class Role {
    Admin,
    Member
}
