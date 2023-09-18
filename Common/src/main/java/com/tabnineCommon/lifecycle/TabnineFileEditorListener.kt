package com.tabnineCommon.lifecycle

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.util.concurrency.AppExecutorUtil
import com.tabnineCommon.binary.requests.fileLifecycle.PrefetchRequest
import com.tabnineCommon.general.DependencyContainer

class TabnineFileEditorListener : FileEditorManagerListener {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    override fun selectionChanged(event: FileEditorManagerEvent) {
        super.selectionChanged(event)
        AppExecutorUtil.getAppExecutorService().execute {
            sendPrefetchRequest(event.newEditor?.file?.path)
        }
    }

    private fun sendPrefetchRequest(path: String?) {
        if (path == null) {
            Logger.getInstance(javaClass).warn("Failed to find path for selected file, skipping prefetch request")
            return
        }

        binaryRequestFacade.executeRequest(PrefetchRequest(path))
    }
}
