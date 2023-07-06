package com.tabnine.testUtils

import com.tabnineCommon.binary.BinaryProcessGateway
import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.requests.capabilities.CapabilitiesRequest
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.StaticConfig.wrapWithBinaryRequest
import org.mockito.Mockito.`when`
import java.util.concurrent.ArrayBlockingQueue

object MockBinaryResponse {
    @JvmStatic
    fun <R> mockBinaryResponse(
        binaryProcessGateway: BinaryProcessGateway,
        request: BinaryRequest<R>,
        response: Any
    ) {
        val writtenValueQueue = ArrayBlockingQueue<String>(1)

        `when`(
            binaryProcessGateway.writeRequest(
                DependencyContainer.instanceOfGson()
                    .toJson(wrapWithBinaryRequest(request.serialize())) + "\n"
            )
        ).then {
            writtenValueQueue.put(DependencyContainer.instanceOfGson().toJson(response))
        }

        `when`(binaryProcessGateway.readRawResponse()).then {
            writtenValueQueue.poll()
        }
    }

    @JvmStatic
    fun mockCapabilities(
        binaryProcessGateway: BinaryProcessGateway,
        experimentSource: String,
        enabledFeatures: List<String> = emptyList()
    ) {
        val response = mapOf(
            "enabled_features" to enabledFeatures,
            "experiment_source" to experimentSource
        )

        mockBinaryResponse(binaryProcessGateway, CapabilitiesRequest(), response)
    }
}
