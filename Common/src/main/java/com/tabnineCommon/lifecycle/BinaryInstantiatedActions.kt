package com.tabnineCommon.lifecycle

import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.config.ConfigRequest

class BinaryInstantiatedActions(val binaryRequestFacade: BinaryRequestFacade) {
    fun openHub() {
        binaryRequestFacade.executeRequest(ConfigRequest())
    }
}
