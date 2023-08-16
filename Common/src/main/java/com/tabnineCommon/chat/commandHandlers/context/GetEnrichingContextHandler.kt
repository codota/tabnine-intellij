package com.tabnineCommon.chat.commandHandlers.context

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.chat.commandHandlers.context.workspace.WorkspaceCommand
import com.tabnineCommon.chat.commandHandlers.context.workspace.WorkspaceContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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
        val editor = getEditorFromProject(project).get() ?: return EnrichingContextResponsePayload()

        val enrichingContextData = contextTypesSet.map {
            when (it) {
                EnrichingContextType.Editor -> EditorContext.createFuture(editor)
                EnrichingContextType.Workspace -> WorkspaceContext.createFuture(
                    editor,
                    project,
                    payload.workspaceCommands ?: emptyList()
                )

                EnrichingContextType.Diagnostics -> DiagnosticsContext.createFuture(editor, project)
            }
        }

        CompletableFuture.allOf(*enrichingContextData.toTypedArray()).get(3, TimeUnit.SECONDS)

        return EnrichingContextResponsePayload(enrichingContextData.mapNotNull { it.get() })
    }

    override fun deserializeRequest(data: JsonElement?): EnrichingContextRequestPayload? {
        return gson.fromJson(data, EnrichingContextRequestPayload::class.java)
    }
}
