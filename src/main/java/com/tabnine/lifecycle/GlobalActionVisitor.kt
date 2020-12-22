package com.tabnine.lifecycle

import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.config.ConfigRequest

class GlobalActionVisitor(val binaryRequestFacade: BinaryRequestFacade) {
    fun none() {
        // Nothing to do.
    }

    fun openHub() {
        binaryRequestFacade.executeRequest(ConfigRequest())
    }

    fun openLp() {
        // Nothing to do, the binary takes care of it.
    }

    fun openBuy() {
        // Nothing to do, the binary takes care of it.
    }
}
