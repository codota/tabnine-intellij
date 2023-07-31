package com.tabnineCommon.chat.commandHandlers.context

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.chat.commandHandlers.context.workspace.WorkspaceCommand
import com.tabnineCommon.chat.commandHandlers.context.workspace.WorkspaceContext

enum class EnrichingContextType {
    Editor,
    Workspace,
    Diagnostics,
}

data class EnrichingContextRequestPayload(
    val contextTypes: List<EnrichingContextType>,
    val workspaceCommands: List<WorkspaceCommand>? = null,
)

interface EnrichingContextData

data class EnrichingContextResponsePayload(private val enrichingContextData: List<EnrichingContextData> = emptyList())

class GetEnrichingContextHandler(gson: Gson) :
    ChatMessageHandler<EnrichingContextRequestPayload, EnrichingContextResponsePayload>(gson) {
    override fun handle(payload: EnrichingContextRequestPayload?, project: Project): EnrichingContextResponsePayload {
        val contextTypesSet = payload?.contextTypes?.toSet() ?: return EnrichingContextResponsePayload()
        val editor = getEditorFromProject(project) ?: return EnrichingContextResponsePayload()

        val enrichingContextData = contextTypesSet.mapNotNull {
            when (it) {
                EnrichingContextType.Editor -> EditorContext.create(editor)
                EnrichingContextType.Workspace -> WorkspaceContext.create(editor, project, payload?.workspaceCommands ?: emptyList())
                EnrichingContextType.Diagnostics -> DiagnosticsContext.create(editor, project)
            }
        }

        return EnrichingContextResponsePayload(enrichingContextData)
    }

    override fun deserializeRequest(data: JsonElement?): EnrichingContextRequestPayload? {
        return gson.fromJson(data, EnrichingContextRequestPayload::class.java)
    }
}
