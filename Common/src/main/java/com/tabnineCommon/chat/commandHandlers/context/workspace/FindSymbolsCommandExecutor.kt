package com.tabnineCommon.chat.commandHandlers.context.workspace

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.context.BasicContextCache
import com.tabnineCommon.chat.commandHandlers.utils.SymbolsResolver

class FindSymbolsCommandExecutor : CommandsExecutor {
    override fun execute(arg: String, editor: Editor, project: Project): List<String> {
        val basicContext = BasicContextCache.get(editor)
        if (basicContext == null || basicContext.language.isNullOrBlank()) {
            Logger.getInstance(javaClass).warn("Could not obtain basic context, skipping findSymbols command execution")
            return emptyList()
        }

        return SymbolsResolver.resolveSymbols(project, editor.document, arg, 5)
            .filter { !it.text.isNullOrBlank() }
            .take(2)
            .map { "file: ${it.relativePath}\n```${basicContext.language}\n${it.text}\n```" }
    }
}
