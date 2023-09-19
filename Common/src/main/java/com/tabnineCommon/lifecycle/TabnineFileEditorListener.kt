package com.tabnineCommon.lifecycle

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import com.tabnineCommon.binary.requests.analytics.EventRequest
import com.tabnineCommon.binary.requests.fileLifecycle.PrefetchRequest
import com.tabnineCommon.general.DependencyContainer

class TabnineFileEditorListener : FileEditorManagerListener {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    override fun selectionChanged(event: FileEditorManagerEvent) {
        super.selectionChanged(event)
        val file = event.newEditor?.file

        if (file == null) {
            Logger.getInstance(javaClass).warn("Failed to find path for selected file, skipping prefetch request")
            return
        }

        AppExecutorUtil.getAppExecutorService().execute {
            handleFileChanged(file)
        }
    }

    private fun handleFileChanged(file: VirtualFile) {
        val isDirty = FileDocumentManager.getInstance().isFileModified(file)
        binaryRequestFacade.executeRequest(
            EventRequest(
                "active_text_editor_changed",
                mapOf("isDirty" to isDirty.toString())
            )
        )

        binaryRequestFacade.executeRequest(PrefetchRequest(file.path))
    }

    companion object {
        fun registerListener() {
            ApplicationManager.getApplication().messageBus.connect().subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER, TabnineFileEditorListener()
            )
        }
    }
}
