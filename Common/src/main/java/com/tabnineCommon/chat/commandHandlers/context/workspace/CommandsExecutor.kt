package com.tabnineCommon.chat.commandHandlers.context.workspace

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

data class ExecutionResult(val result: String, val command: Command)

interface CommandsExecutor {
    companion object {
        private val executors = mutableMapOf<Command, CommandsExecutor>(
            Command.FindSymbols to FindSymbolsCommandExecutor()
        )

        fun execute(command: WorkspaceCommand, editor: Editor, project: Project): ExecutionResult? {
            return executors[command.command]?.execute(command.arg, editor, project)?.let {
                ExecutionResult(it, command.command)
            }
        }
    }

    fun execute(arg: String, editor: Editor, project: Project): String?
}