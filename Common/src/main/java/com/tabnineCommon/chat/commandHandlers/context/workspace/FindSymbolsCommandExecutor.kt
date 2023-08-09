package com.tabnineCommon.chat.commandHandlers.context.workspace

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.utils.StringCaseConverter
import com.tabnineCommon.chat.commandHandlers.utils.SymbolsResolver
import java.util.stream.Stream
import kotlin.streams.toList

private const val MAX_RESULTS_PER_SYMBOL = 5

class FindSymbolsCommandExecutor : CommandsExecutor {
    override fun execute(arg: String, editor: Editor, project: Project): List<String> {
        val camelCaseArg = StringCaseConverter.toCamelCase(arg)
        val snakeCaseArg = StringCaseConverter.toSnakeCase(arg)
        val camelCaseSymbols = SymbolsResolver.resolveSymbols(
            project, editor.document, camelCaseArg,
            MAX_RESULTS_PER_SYMBOL
        )
        val snakeCaseSymbols = SymbolsResolver.resolveSymbols(
            project, editor.document, snakeCaseArg,
            MAX_RESULTS_PER_SYMBOL
        )

        return Stream.concat(camelCaseSymbols.stream(), snakeCaseSymbols.stream())
            .map { "${it.name} - ${it.relativePath}" }
            .toList()
    }
}
