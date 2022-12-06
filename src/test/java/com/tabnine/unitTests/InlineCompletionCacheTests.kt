package com.tabnine.unitTests

import com.tabnine.MockedBinaryCompletionTestCase
import com.tabnine.general.CompletionKind
import com.tabnine.general.CompletionOrigin
import com.tabnine.general.SuggestionTrigger
import com.tabnine.inline.InlineCompletionCache
import com.tabnine.prediction.TabNineCompletion
import org.junit.Test

class InlineCompletionCacheTests : MockedBinaryCompletionTestCase() {

    @Test
    fun shouldRetrieveOnlyRelevantCompletions() {
        InlineCompletionCache.instance.store(
            myFixture.editor,
            listOf(
                createBasicTabnineCompletion(
                    "c",
                    "const",
                ),
                createBasicTabnineCompletion(
                    "c",
                    "cable",
                ),
            )
        )
        val result = InlineCompletionCache.instance.retrieveAdjustedCompletions(myFixture.editor, "o")
        assertEquals(1, result.size)
        assertEquals("co", result[0].oldPrefix)
        assertEquals("co", result[0].cursorPrefix)
    }

    @Test
    fun shouldClearCompletionsCache() {
        InlineCompletionCache.instance.store(
            myFixture.editor,
            listOf(
                createBasicTabnineCompletion(
                    "t",
                    "temp",
                ),
            )
        )
        assertEquals(1, InlineCompletionCache.instance.retrieveAdjustedCompletions(myFixture.editor, "e").size)
        InlineCompletionCache.instance.clear(myFixture.editor)
        assertEquals(0, InlineCompletionCache.instance.retrieveAdjustedCompletions(myFixture.editor, "e").size)
    }

    private fun createBasicTabnineCompletion(
        oldPrefix: String,
        newPrefix: String,
    ): TabNineCompletion {
        return TabNineCompletion(
            oldPrefix,
            newPrefix,
            "",
            "",
            0,
            oldPrefix,
            "",
            CompletionOrigin.UNKNOWN,
            CompletionKind.Classic,
            true,
            null,
            SuggestionTrigger.DocumentChanged
        )
    }
}
