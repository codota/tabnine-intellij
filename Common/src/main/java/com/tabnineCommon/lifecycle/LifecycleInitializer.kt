package com.tabnineCommon.lifecycle

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener

fun initializeLifecycleEndpoints() {
    ServiceManager.getService(BinaryStateService::class.java).startUpdateLoop()
    ApplicationManager.getApplication().messageBus.connect().subscribe(
        FileEditorManagerListener.FILE_EDITOR_MANAGER, TabnineFileEditorListener()
    )
}
