package com.tabnine.plugin.completionPostProcess

import com.tabnine.binary.requests.autocomplete.postprocess
import org.junit.Test

class Tabs {
    @Test
    fun shouldReindentAndTrimCorrectlyWhereIndentationIsReseeding() {
        val request = request("def a():\n\t")
        val response = response("if x > 2:\n\t\treturn x\n\t.return None\ndef b():\n\treturn 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None")
    }

    @Test
    fun shouldReindentAndNotTrimWhereIndentationIsNotReseeding() {
        val request = request("def a():\n\t")
        val response = response("if x > 2:\n\t\treturn x")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x")
    }

    @Test
    fun shouldReindentAndNotTrimWhereRequestIndentationIsZero() {
        val request = request("def a():\n")
        val response = response("if x > 2:\n\t\treturn x\n\t.return None\ndef b():\n\treturn 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None\ndef b():\n  return 3")
    }

    @Test
    fun shouldReindentAndTrimWhereIndentationIsZeroOnTheFirstLine() {
        val request = request("def a():\n\t")
        val response = response("\ndef b():\n\treturn 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "")
    }

    @Test
    fun shouldDoNothingWhereResponseIsOneLine() {
        val request = request("def a():\n\t")
        val response = response("return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "return 3")
    }
}
