package com.tabnineCommon.testUtils

import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.general.Utils
import org.mockito.ArgumentMatchers
import org.mockito.MockedStatic
import org.mockito.Mockito
import java.util.concurrent.Future

fun mockThreadResponseWith(mockedFuture: Future<*>?): MockedStatic<Utils> {
    val mockStatic = Mockito.mockStatic(Utils::class.java)
    mockStatic.`when`<Any?> { Utils.executeThread(ArgumentMatchers.any()) }.then {
        it.getArgument(0, Runnable::class.java).run()
        mockedFuture
    }
    return mockStatic
}

fun mockExponentialBackoffWith(mockedBackoffTimeMs: Int): MockedStatic<StaticConfig> {
    val mockStatic = Mockito.mockStatic(StaticConfig::class.java)
    mockStatic.`when`<Any?> { StaticConfig.exponentialBackoff(ArgumentMatchers.anyInt()) }.thenReturn(mockedBackoffTimeMs)
    return mockStatic
}
