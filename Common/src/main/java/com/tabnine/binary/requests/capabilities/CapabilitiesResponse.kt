package com.tabnine.binary.requests.capabilities

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryResponse
import com.tabnine.capabilities.Capability

data class CapabilitiesResponse(
    @SerializedName("enabled_features")
    var enabledFeatures: List<Capability>? = null,
) : BinaryResponse
