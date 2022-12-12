package com.tabnine.inline

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.tabnine.prediction.TabNineCompletion
import java.util.stream.Collectors

class InlineCompletionCache {

    companion object {
        @JvmStatic
        val instance = InlineCompletionCache()

        private val INLINE_COMPLETIONS_LAST_RESULT = Key.create<List<TabNineCompletion>>("INLINE_COMPLETIONS_LAST_RESULT")
    }

    fun store(editor: Editor, completions: List<TabNineCompletion>) {
        editor.putUserData(INLINE_COMPLETIONS_LAST_RESULT, completions)
    }

    fun retrieveAdjustedCompletions(editor: Editor, userInput: String): List<TabNineCompletion> {
        val completions = editor.getUserData(INLINE_COMPLETIONS_LAST_RESULT)
            ?: return emptyList()
        return completions.stream()
            .filter { completion: TabNineCompletion -> completion.suffix.startsWith(userInput) }
            .map { completion: TabNineCompletion ->
                completion.createAdjustedCompletion(
                    completion.oldPrefix + userInput,
                    completion.cursorPrefix + userInput
                )
            }
            .collect(Collectors.toList())
    }

    fun clear(editor: Editor) {
        editor.putUserData(INLINE_COMPLETIONS_LAST_RESULT, null)
    }
}
