package com.tabnine.unitTests

import com.tabnine.inline.render.experimental.FirstLineRendering
import com.tabnine.inline.render.experimental.RenderingInstructions
import com.tabnine.inline.render.experimental.determineRendering
import org.junit.Test

class InlineStringProcessorTests {
    @Test
    fun testNoRendering() {
        assert(determineRendering(listOf(""), "") == RenderingInstructions(FirstLineRendering.None, false))
    }

    @Test
    fun testSimpleString() {
        assert(
            determineRendering(listOf("simple case"), "") == RenderingInstructions(
                FirstLineRendering.NoSuffix,
                false
            )
        )
    }

    @Test
    fun testStringWithSuffix() {
        assert(
            determineRendering(
                listOf("simple case"),
                "ca"
            ) == RenderingInstructions(FirstLineRendering.BeforeAndAfterSuffix, false)
        )
    }

    @Test
    fun testSuffixOnly() {
        assert(
            determineRendering(listOf("simple case"), "sim") == RenderingInstructions(
                FirstLineRendering.SuffixOnly,
                false
            )
        )
    }

    @Test
    fun testFirstLineAndBlock() {
        assert(
            determineRendering(listOf("simple text", "some more text"), "") == RenderingInstructions(
                FirstLineRendering.NoSuffix,
                true
            )
        )
    }

    @Test
    fun testBlockOnly() {
        assert(
            determineRendering(listOf("", "some text"), "") == RenderingInstructions(
                FirstLineRendering.None,
                true
            )
        )
    }
}
