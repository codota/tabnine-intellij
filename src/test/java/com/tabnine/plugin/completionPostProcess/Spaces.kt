package com.tabnine.plugin.completionPostProcess

import com.tabnine.binary.requests.autocomplete.postprocess
import org.junit.Test

class Spaces {
    @Test
    fun shouldTrimCorrectlyWhereIndentationIsReseeding() {
        val request = request("def a():\n  ")
        val response = response("if x > 2:\n    return x\n  .return None\ndef b():\n  return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None")
    }

    @Test
    fun shouldNotTrimWhereIndentationIsNotReseeding() {
        val request = request("def a():\n\t")
        val response = response("if x > 2:\n    return x")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x")
    }

    @Test
    fun shouldNotTrimWhereRequestIndentationIsZero() {
        val request = request("def a():\n")
        val response = response("if x > 2:\n    return x\n  .return None\ndef b():\n  return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None\ndef b():\n  return 3")
    }

    @Test
    fun shouldTrimWhereIndentationIsZeroOnTheFirstLine() {
        val request = request("def a():\n  ")
        val response = response("\ndef b():\n  return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "")
    }

    @Test
    fun shouldDoNothingWhereResponseIsOneLine() {
        val request = request("def a():\n  ")
        val response = response("return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "return 3")
    }
}
