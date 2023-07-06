package com.tabnineCommon.binary.requests.capabilities

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryResponse
import com.tabnineCommon.capabilities.Capability

data class CapabilitiesResponse(
    @SerializedName("enabled_features")
    var enabledFeatures: List<Capability>? = null,
    @SerializedName("experiment_source")
    var experimentSource: ExperimentSource? = null
) : BinaryResponse

enum class ExperimentSource {
    @SerializedName("API")
    API,

    @SerializedName("APIErrorResponse")
    APIErrorResponse,

    @SerializedName("Hardcoded")
    Hardcoded,

    @SerializedName("Unknown")
    Unknown;

    fun isRemoteBasedSource() = this == API || this == APIErrorResponse
}
