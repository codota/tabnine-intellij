package com.tabnine-common.plugin

import com.tabnine.MockedBinaryCompletionTestCase
import com.tabnine.capabilities.SuggestionsMode
import com.tabnine.testUtils.TestData
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

const val SOME_PREFIX = "\nt"

class HybridModeTests : MockedBinaryCompletionTestCase() {

    @Before
    fun init() {
        Mockito.`when`(suggestionsModeServiceMock.getSuggestionMode()).thenReturn(SuggestionsMode.HYBRID)
    }

    private fun mockCompletionResponse(response: String) {
        Mockito.`when`(binaryProcessGatewayMock.readRawResponse())
            .thenReturn(setOldPrefixFor(response, "t"))
    }

    @Test
    fun shouldShowOnlyInlineCompletionWhenCompletionHasSnippet() {
        mockCompletionResponse(TestData.MULTI_LINE_SNIPPET_PREDICTION_RESULT)
        type(SOME_PREFIX)

        assertEquals("Incorrect inline completion", "emp\ntemp2", getTabnineCompletionContent(myFixture))
        assert(popupCompletions.isEmpty())
    }

    @Test
    fun shouldShowOnlyPopupCompletionWhenCompletionIsNotSnippet() {
        mockCompletionResponse(TestData.THIRD_PREDICTION_RESULT)
        type(SOME_PREFIX)

        assertNull("Should have not shown inline completion", getTabnineCompletionContent(myFixture))
        assertEquals("Incorrect popup completion", listOf("temp", "temporary", "temporary file"), popupCompletions)
    }
}
