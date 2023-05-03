package com.tabnine.lifecycle

import com.tabnine.binary.requests.config.ConfigRequest
import com.tabnineCommon.binary.BinaryRequestFacade

class BinaryInstantiatedActions(private val binaryRequestFacade: BinaryRequestFacade) {
    fun openHub() {
        binaryRequestFacade.executeRequest(ConfigRequest())
    }
}
