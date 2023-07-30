package com.tabnineCommon.chat.commandHandlers.context

import com.google.gson.annotations.SerializedName

enum class Command {
    @SerializedName("findSymbols")
    FindSymbols
}

data class WorkspaceCommand(val command: Command, val arg: String)

data class WorkspaceContext(
    private val symbols: List<String> = emptyList(),
) : EnrichingContextData {
    private val type: EnrichingContextType = EnrichingContextType.Workspace

    companion object {
        fun create(): WorkspaceContext? {
            // TODO: implement
            return null
        }
    }
}
