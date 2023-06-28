package com.tabnine.binary.requests.fileMetadata

import com.google.gson.JsonObject
import com.tabnineCommon.binary.BinaryRequest

data class FileMetadataRequest(
    val path: String
) : BinaryRequest<JsonObject> {
    override fun response(): Class<JsonObject> {
        return JsonObject::class.java
    }

    override fun serialize(): Any {
        return mapOf("FileMetadata" to this)
    }
}
