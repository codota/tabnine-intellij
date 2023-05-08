package com.tabnineCommon.binary.requests.config

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryResponse
import com.tabnineCommon.general.ServiceLevel

data class StateResponse(
    @SerializedName("service_level")
    var serviceLevel: ServiceLevel? = null,
    @SerializedName("process_state")
    val processState: ProcessState? = null,
    @SerializedName("installation_time")
    val installationTime: String? = null,
    @SerializedName("cloud_connection_health_status")
    val cloudConnectionHealthStatus: CloudConnectionHealthStatus = CloudConnectionHealthStatus.Ok,
    @SerializedName("is_logged_in")
    val isLoggedIn: Boolean? = null,
    @SerializedName("user_name")
    var userName: String? = null,
) : BinaryResponse

data class ProcessState(
    val globalRestartStatus: Map<String, RestartStatus>? = null
)

data class RestartStatus(
    val setOn: String,
    val restartOn: String?,
    val value: String
)

enum class CloudConnectionHealthStatus {
    Ok,
    Failed
}
