package com.tabnine-common.testUtils

import com.tabnine.general.StaticConfig
import org.mockito.stubbing.Answer

fun returnAfterTimeout(result: String): Answer<Any> {
    return returnAfter(result, StaticConfig.COMPLETION_TIME_THRESHOLD + TestData.EPSILON)
}

fun returnAfter(result: String, millis: Int): Answer<Any> {
    return Answer<Any> {
        Thread.sleep(millis.toLong())
        result
    }
}
