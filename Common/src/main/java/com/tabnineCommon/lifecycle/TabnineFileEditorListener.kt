package com.tabnineCommon.lifecycle

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.tabnineCommon.binary.requests.fileLifecycle.PrefetchRequest
import com.tabnineCommon.general.DependencyContainer

class TabnineFileEditorListener : FileEditorManagerListener {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    override fun selectionChanged(event: FileEditorManagerEvent) {
        super.selectionChanged(event)
        val path = event.newEditor?.file?.path
        if (path == null) {
            Logger.getInstance(javaClass).warn("Failed to find path for selected file, skipping prefetch request")
            return
        }

        binaryRequestFacade.executeRequest(PrefetchRequest(path))
    }
}
