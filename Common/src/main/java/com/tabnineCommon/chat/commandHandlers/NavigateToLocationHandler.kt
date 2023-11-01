package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.tabnineCommon.chat.commandHandlers.utils.ActionPermissions
import com.tabnineCommon.chat.commandHandlers.utils.AsyncAction

data class TextLineRange(val startLine: Int, val endLine: Int)
data class NavigateToLocationRequest(val path: String, val range: TextLineRange)

class NavigateToLocationHandler(gson: Gson) : ChatMessageHandler<NavigateToLocationRequest, Unit>(gson) {
    override fun handle(payload: NavigateToLocationRequest?, project: Project) {
        if (payload == null) return
        AsyncAction(ActionPermissions.WRITE).execute {
            val file = LocalFileSystem.getInstance().findFileByPath(payload.path) ?: return@execute
            val fileEditor =
                FileEditorManager.getInstance(project).openFile(file, true)[0]
            val dataContext = DataManager.getInstance().getDataContext(fileEditor.component)
            val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return@execute

            val startOffset = editor.document.getLineStartOffset(payload.range.startLine)
            val endOffset = editor.document.getLineEndOffset(payload.range.endLine)
            val startLogicalPosition = editor.offsetToLogicalPosition(startOffset)
            editor.caretModel.moveToLogicalPosition(startLogicalPosition)
            editor.selectionModel.setSelection(startOffset, endOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        }.get()
    }

    override fun deserializeRequest(data: JsonElement?): NavigateToLocationRequest? {
        return gson.fromJson(data, NavigateToLocationRequest::class.java)
    }
}
