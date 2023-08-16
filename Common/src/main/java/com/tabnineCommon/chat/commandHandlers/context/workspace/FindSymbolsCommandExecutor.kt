package com.tabnineCommon.chat.commandHandlers.context.workspace

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.context.BasicContextCache
import com.tabnineCommon.chat.commandHandlers.utils.StringCaseConverter
import com.tabnineCommon.chat.commandHandlers.utils.SymbolsResolver
import com.tabnineCommon.chat.commandHandlers.utils.submitReadAction
import java.util.concurrent.CompletableFuture

private const val MAX_RESULTS_PER_SYMBOL = 5

class FindSymbolsCommandExecutor : CommandsExecutor {
    override fun execute(arg: String, editor: Editor, project: Project): List<String> {
        val basicContext = BasicContextCache.get(editor)
        if (basicContext == null || basicContext.language.isNullOrBlank()) {
            Logger.getInstance(javaClass).warn("Could not obtain basic context, skipping findSymbols command execution")
            return emptyList()
        }

        val camelCaseArg = StringCaseConverter.toCamelCase(arg)
        val snakeCaseArg = StringCaseConverter.toSnakeCase(arg)

        val tasks = listOf(
            submitReadAction {
                SymbolsResolver.resolveSymbols(
                    project, editor.document, camelCaseArg,
                    MAX_RESULTS_PER_SYMBOL
                )
            },
            submitReadAction {
                SymbolsResolver.resolveSymbols(
                    project, editor.document, snakeCaseArg,
                    MAX_RESULTS_PER_SYMBOL
                )
            }
        )

        CompletableFuture.allOf(*tasks.toTypedArray()).get()

        return tasks.asSequence().map { it.get() }.flatten()
            .filter { !it.text.isNullOrBlank() }
            .take(2)
            .map { "file: ${it.relativePath}\n```${basicContext.language.toLowerCase()}\n${it.text}\n```" }.toList()
    }
}
