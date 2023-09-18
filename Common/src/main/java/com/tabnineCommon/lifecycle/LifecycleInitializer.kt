package com.tabnineCommon.lifecycle

import com.intellij.openapi.components.ServiceManager

fun initializeLifecycleEndpoints() {
    ServiceManager.getService(BinaryStateService::class.java).startUpdateLoop()
    TabnineFileEditorListener.registerListener()
}
