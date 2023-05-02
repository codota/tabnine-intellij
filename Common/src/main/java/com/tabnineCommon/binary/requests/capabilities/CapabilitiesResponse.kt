package com.tabnineCommon.binary.requests.capabilities

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryResponse
import com.tabnineCommon.capabilities.Capability

data class CapabilitiesResponse(
    @SerializedName("enabled_features")
    var enabledFeatures: List<Capability>? = null,
) : BinaryResponse
