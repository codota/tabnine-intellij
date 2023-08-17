package com.tabnineCommon.chat.commandHandlers.context.workspace

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.utils.ActionPermissions
import com.tabnineCommon.chat.commandHandlers.utils.AsyncAction
import com.tabnineCommon.chat.commandHandlers.utils.StringCaseConverter
import com.tabnineCommon.chat.commandHandlers.utils.SymbolsResolver
import java.util.concurrent.CompletableFuture

private const val MAX_RESULTS_PER_SYMBOL = 5

class FindSymbolsCommandExecutor : CommandsExecutor {
    override fun execute(arg: String, editor: Editor, project: Project): List<String> {
        val camelCaseArg = StringCaseConverter.toCamelCase(arg)
        val snakeCaseArg = StringCaseConverter.toSnakeCase(arg)

        val tasks = listOf(
            AsyncAction(ActionPermissions.READ).execute {
                SymbolsResolver.resolveSymbols(
                    project, editor.document, camelCaseArg,
                    MAX_RESULTS_PER_SYMBOL
                )
            },
            AsyncAction(ActionPermissions.READ).execute {
                SymbolsResolver.resolveSymbols(
                    project, editor.document, snakeCaseArg,
                    MAX_RESULTS_PER_SYMBOL
                )
            }
        )

        CompletableFuture.allOf(*tasks.toTypedArray()).get()

        return tasks.map { it.get() }.flatten()
            .map { "${it.name} - ${it.relativePath}" }
            .toList()
    }
}
