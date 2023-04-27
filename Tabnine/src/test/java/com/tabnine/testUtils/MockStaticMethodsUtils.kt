package com.tabnine.testUtils

import com.intellij.mock.MockApplication
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.tabnineCommon.general.IProviderOfThings
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

fun mockedIProviderOfThingsService(): IProviderOfThings {
    val disposableMock = Mockito.mock(Disposable::class.java)
    val application = MockApplication(disposableMock)
    ApplicationManager.setApplication(application, disposableMock)
    val implementation = Mockito.mock(IProviderOfThings::class.java)
    application.registerService(IProviderOfThings::class.java, implementation)
    return implementation
}
