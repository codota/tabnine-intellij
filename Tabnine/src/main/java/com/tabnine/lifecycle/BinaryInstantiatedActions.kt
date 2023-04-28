package com.tabnine.lifecycle

import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.config.ConfigRequest
import com.tabnineCommon.lifecycle.IBinaryInstantiatedActions

class BinaryInstantiatedActions(private val binaryRequestFacade: BinaryRequestFacade) : IBinaryInstantiatedActions {
    override fun openHub() {
        binaryRequestFacade.executeRequest(ConfigRequest())
    }
}
