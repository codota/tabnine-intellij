package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project

data class NonExistingCommandResponsePayload(private val error: String)

class DefaultCommandHandler(gson: Gson, private val command: String) : ChatMessageHandler<Unit, NonExistingCommandResponsePayload>(gson) {
    override fun handle(payload: Unit?, project: Project): NonExistingCommandResponsePayload {
        return NonExistingCommandResponsePayload("Command $command doesn't have a handler")
    }

    override fun deserializeRequest(data: JsonElement?) {}
}
