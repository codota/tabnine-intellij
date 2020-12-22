package com.tabnine.lifecycle

import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.config.ConfigRequest

class BinaryInstantiatedActions(val binaryRequestFacade: BinaryRequestFacade) {
    fun openHub() {
        binaryRequestFacade.executeRequest(ConfigRequest())
    }
}
