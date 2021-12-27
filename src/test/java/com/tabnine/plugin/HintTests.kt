package com.tabnine.plugin

import com.intellij.util.Alarm
import com.tabnine.capabilities.CapabilitiesService
import com.tabnine.capabilities.Capability
import com.tabnine.capabilities.SuggestionsMode
import com.tabnine.inline.CompletionPreview
import com.tabnine.integration.MockedBinaryCompletionTestCase
import com.tabnine.testutils.TestData.MULTI_LINE_PREDICTION_RESULT
import com.tabnine.testutils.TestData.THIRD_PREDICTION_RESULT
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.MockedStatic
import org.mockito.Mockito

class HintTests : MockedBinaryCompletionTestCase() {
    private var suggestionsModeStaticMock: MockedStatic<SuggestionsMode>? = null
    private var capabilitiesStaticMock: MockedStatic<CapabilitiesService>? = null
    private var capabilitiesMock = Mockito.mock(CapabilitiesService::class.java)
    private val alarmMock = Mockito.mock(Alarm::class.java)

    @Before
    fun init() {
        suggestionsModeStaticMock = Mockito.mockStatic(SuggestionsMode::class.java)
        capabilitiesStaticMock = Mockito.mockStatic(CapabilitiesService::class.java)
    }

    @After
    fun clear() {
        suggestionsModeStaticMock?.close()
        capabilitiesStaticMock?.close()
    }

    @Throws(java.lang.Exception::class)
    private fun configureInlineTestForAlpha(binaryResult: String) {
        Mockito.`when`(binaryProcessGatewayMock.readRawResponse()).thenReturn(binaryResult)
        Mockito.`when`(capabilitiesMock.isCapabilityEnabled(Capability.ALPHA)).thenReturn(true)
        capabilitiesStaticMock!!.`when`<Any> { CapabilitiesService.getInstance() }.thenReturn(capabilitiesMock)
        suggestionsModeStaticMock!!.`when`<Any> { SuggestionsMode.getSuggestionMode() }
            .thenReturn(SuggestionsMode.INLINE)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowHintWhenSimpleHintAndCursorMovedToStartOfHint() {
        configureInlineTestForAlpha(THIRD_PREDICTION_RESULT)
        type("\nt")

        val editor = myFixture.editor
        val completionPreview = CompletionPreview.findCompletionPreview(editor)
        if (completionPreview == null) {
            fail("Completion preview not found")
            return
        }

        completionPreview.setAlarm(alarmMock)
        val currentPosition = getCurrentPosition(editor)
        completionPreview.mouseMoved(aCursorMoveEvent(editor, currentPosition))

        Mockito.verify(alarmMock, Mockito.times(1))
            .addRequest(ArgumentMatchers.any(Runnable::class.java), ArgumentMatchers.anyInt())
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowHintWhenMultilineHintAndCursorMovedToSecondRow() {
        configureInlineTestForAlpha(MULTI_LINE_PREDICTION_RESULT)
        type("\nt")

        val editor = myFixture.editor
        val completionPreview = CompletionPreview.findCompletionPreview(editor)
        if (completionPreview == null) {
            fail("Completion preview not found")
            return
        }

        completionPreview.setAlarm(alarmMock)
        val nextLineStartPosition = getCurrentPosition(editor)
        nextLineStartPosition.x = 0
        nextLineStartPosition.y += 1

        completionPreview.mouseMoved(aCursorMoveEvent(editor, nextLineStartPosition))

        Mockito.verify(alarmMock, Mockito.times(1))
            .addRequest(ArgumentMatchers.any(Runnable::class.java), ArgumentMatchers.anyInt())
    }
}
