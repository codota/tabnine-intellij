package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.utils.ActionPermissions
import com.tabnineCommon.chat.commandHandlers.utils.AsyncAction

abstract class ChatMessageHandler<RequestPayload, ResponsePayload>(protected val gson: Gson) {
    fun handleRaw(data: JsonElement?, project: Project): ResponsePayload? {
        val payload = deserializeRequest(data)
        return handle(payload, project)
    }

    protected fun getEditorFromProject(project: Project): Editor? {
        return AsyncAction(ActionPermissions.WRITE).execute {
            try {
                val fileEditor = FileEditorManager.getInstance(project).selectedEditor ?: return@execute null
                val dataContext = DataManager.getInstance().getDataContext(fileEditor.component)

                CommonDataKeys.EDITOR.getData(dataContext)
            } catch (e: Exception) {
                Logger.getInstance(javaClass).error("Failed to get editor from project: ", e)
                null
            }
        }.get()
    }

    abstract fun handle(payload: RequestPayload?, project: Project): ResponsePayload?

    abstract fun deserializeRequest(data: JsonElement?): RequestPayload?
}
