package com.tabnineSelfHosted.lifecycle

import com.intellij.openapi.diagnostic.Logger
import com.tabnineCommon.lifecycle.IBinaryInstantiatedActions

class BinaryInstantiatedActions : IBinaryInstantiatedActions {
    override fun openHub() {
        Logger.getInstance(BinaryInstantiatedActions::class.java).info("Never open the hub on self hosted")
    }
}
