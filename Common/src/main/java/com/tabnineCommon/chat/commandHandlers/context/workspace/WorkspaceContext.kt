package com.tabnineCommon.chat.commandHandlers.context.workspace

import com.google.gson.annotations.SerializedName
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.context.EnrichingContextData
import com.tabnineCommon.chat.commandHandlers.context.EnrichingContextType
import com.tabnineCommon.chat.commandHandlers.utils.ActionPermissions
import com.tabnineCommon.chat.commandHandlers.utils.AsyncAction
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

enum class Command {
    @SerializedName("findSymbols")
    FindSymbols
}

data class WorkspaceCommand(val command: Command, val arg: String)

data class WorkspaceContext(
    private val symbols: List<Any> = emptyList(),
) : EnrichingContextData {
    // Used for serialization - do not remove
    private val type: EnrichingContextType = EnrichingContextType.Workspace

    companion object {
        fun createFuture(editor: Editor, project: Project, workspaceCommands: List<WorkspaceCommand>): CompletableFuture<WorkspaceContext> {
            return AsyncAction(ActionPermissions.READ).execute {
                create(editor, project, workspaceCommands)
            }
        }

        private fun create(editor: Editor, project: Project, workspaceCommands: List<WorkspaceCommand>): WorkspaceContext {
            if (workspaceCommands.isEmpty()) return WorkspaceContext(emptyList())

            val tasks = workspaceCommands.map { workspaceCommand ->
                AsyncAction(ActionPermissions.READ).execute {
                    CommandsExecutor.execute(workspaceCommand, editor, project)
                }
            }

            return try {
                CompletableFuture.allOf(*tasks.toTypedArray()).get(2500, TimeUnit.MILLISECONDS)

                val symbols = mutableListOf<Any>()

                tasks.mapNotNull { it.get() }.forEach { executionResult ->
                    when (executionResult.command) {
                        Command.FindSymbols -> symbols.addAll(executionResult.result)
                    }
                }

                WorkspaceContext(symbols.distinct())
            } catch (e: TimeoutException) {
                Logger.getInstance(WorkspaceContext::class.java)
                    .warn("Timeout while waiting for workspace commands to execute, continuing without workspace symbols")
                WorkspaceContext(emptyList())
            }
        }
    }
}
