package com.tabnine.unitTests

import com.tabnine.MockedBinaryCompletionTestCase
import com.tabnineCommon.binary.requests.autocomplete.CompletionMetadata
import com.tabnineCommon.general.CompletionKind
import com.tabnineCommon.general.CompletionOrigin
import com.tabnineCommon.general.SuggestionTrigger
import com.tabnineCommon.inline.InlineCompletionCache
import com.tabnineCommon.prediction.TabNineCompletion
import org.junit.Test

class InlineCompletionCacheTests : MockedBinaryCompletionTestCase() {

    @Test
    fun givenAPrefixThenReturnOnlySuggestionsThatStartWithThisPrefix() {
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
    fun givenExistingCompletionInCacheAndCallWithEmptyInputThenReturnAllOfThem() {
        InlineCompletionCache.instance.store(
            myFixture.editor,
            listOf(
                createBasicTabnineCompletion(
                    "a",
                    "abc",
                ),
                createBasicTabnineCompletion(
                    "d",
                    "def",
                ),
            )
        )
        val result = InlineCompletionCache.instance.retrieveAdjustedCompletions(myFixture.editor, "")
        assertEquals(2, result.size)
    }

    @Test
    fun givenExistingCompletionInCacheAndCallClearCacheThenCacheShouldBeEmpty() {
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

    @Test
    fun givenEmptyCompletionInCacheAndCallRetrieveWithInputThenShouldReturnEmptyList() {
        InlineCompletionCache.instance.store(
            myFixture.editor,
            listOf(
                createBasicTabnineCompletion(
                    "",
                    "",
                ),
            )
        )
        assertEquals(0, InlineCompletionCache.instance.retrieveAdjustedCompletions(myFixture.editor, "e").size)
    }

    private fun createBasicTabnineCompletion(
        oldPrefix: String,
        newPrefix: String,
    ): TabNineCompletion {
        val someMetadata = CompletionMetadata(
            CompletionOrigin.UNKNOWN,
            "0.345",
            CompletionKind.Classic,
            null,
            null,
            false
        )
        return TabNineCompletion(
            oldPrefix,
            newPrefix,
            "",
            "",
            0,
            oldPrefix,
            "",
            someMetadata,
            SuggestionTrigger.DocumentChanged
        )
    }
}
