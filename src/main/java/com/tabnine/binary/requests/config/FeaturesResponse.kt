package com.tabnine.binary.requests.config

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryResponse

data class FeaturesResponse(
        @SerializedName("enabled_features")
        val enabledFeatues: Array<String>) : BinaryResponse