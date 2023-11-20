package com.tabnineCommon.binary.requests.fileLifecycle

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.exceptions.TabNineInvalidResponseException
import com.tabnineCommon.binary.requests.EmptyResponse

data class Workspace(
    @SerializedName("root_paths") private val rootPaths: List<String>
) : BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("Workspace" to this)
    }

    override fun validate(response: EmptyResponse): Boolean {
        return true
    }

    override fun shouldBeAllowed(e: TabNineInvalidResponseException): Boolean {
        return true
    }
}
