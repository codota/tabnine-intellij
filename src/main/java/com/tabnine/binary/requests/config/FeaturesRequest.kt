package com.tabnine.binary.requests.config

import com.tabnine.binary.BinaryRequest
import java.util.*

class FeaturesRequest : BinaryRequest<FeaturesResponse> {
    override fun response(): Class<FeaturesResponse> {
        return FeaturesResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("Features" to Any())
    }

    override fun validate(response: FeaturesResponse): Boolean {
        return true
    }
}