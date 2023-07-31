package com.tabnineCommon.chat.commandHandlers.context.workspace

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.utils.SymbolsResolver

class FindSymbolsCommandExecutor : CommandsExecutor {
    override fun execute(arg: String, editor: Editor, project: Project): String {
        return SymbolsResolver.resolveSymbols(project, editor.document, arg, 5)
            .joinToString("\n") { "${it.name} - ${it.relativePath}" }
    }
}