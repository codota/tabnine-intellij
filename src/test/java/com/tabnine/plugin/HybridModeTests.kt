package com.tabnine.plugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.tabnine.capabilities.SuggestionsMode
import com.tabnine.integration.MockedBinaryCompletionTestCase
import com.tabnine.testUtils.TestData
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

const val SOME_PREFIX = "\nt"

class HybridModeTests : MockedBinaryCompletionTestCase(), Disposable {

    @Before
    fun init() {
        Mockito.`when`(suggestionsModeServiceMock.getSuggestionMode()).thenReturn(SuggestionsMode.HYBRID)
        ApplicationManager.setApplication(mockedApplicationWhichInvokesImmediately(), this)
    }

    @After
    fun clear() {
        Disposer.dispose(this)
    }

    override fun dispose() {}

    private fun mockCompletionResponse(response: String) {
        Mockito.`when`(binaryProcessGatewayMock.readRawResponse())
            .thenReturn(setOldPrefixFor(response, "t"))
    }

    @Test
    fun shouldShowOnlyInlineCompletionWhenCompletionHasSnippet() {
        mockCompletionResponse(TestData.MULTI_LINE_PREDICTION_RESULT)
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
