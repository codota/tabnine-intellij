package com.tabnine.binary.requests.config

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryResponse
import com.tabnine.general.ServiceLevel

data class StateResponse(
    @SerializedName("service_level")
    var serviceLevel: ServiceLevel? = null,

    @SerializedName("api_key")
    var apiKey: String? = null
) : BinaryResponse
