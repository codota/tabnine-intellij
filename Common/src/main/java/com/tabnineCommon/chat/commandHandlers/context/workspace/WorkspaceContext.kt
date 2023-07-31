package com.tabnineCommon.chat.commandHandlers.context.workspace

import com.google.gson.annotations.SerializedName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.context.EnrichingContextData
import com.tabnineCommon.chat.commandHandlers.context.EnrichingContextType
import com.tabnineCommon.chat.commandHandlers.utils.submitReadAction
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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
        fun create(editor: Editor, project: Project, workspaceCommands: List<WorkspaceCommand>): WorkspaceContext {
            if (workspaceCommands.isEmpty()) return WorkspaceContext(emptyList())

            val tasks = workspaceCommands.map { workspaceCommand ->
                submitReadAction {
                    CommandsExecutor.execute(workspaceCommand, editor, project)
                }
            }

            CompletableFuture.allOf(*tasks.toTypedArray()).get(1, TimeUnit.SECONDS)

            val symbols = mutableListOf<String>()

            tasks.mapNotNull { it.get() }.forEach { executionResult ->
                when (executionResult.command) {
                    Command.FindSymbols -> symbols.addAll(executionResult.result)
                }
            }

            return WorkspaceContext(symbols)
        }
    }
}
