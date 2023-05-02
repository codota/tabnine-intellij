package com.tabnine.lifecycle

import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.config.ConfigRequest

class BinaryInstantiatedActions(private val binaryRequestFacade: BinaryRequestFacade) {
    fun openHub() {
        binaryRequestFacade.executeRequest(ConfigRequest())
    }
}
