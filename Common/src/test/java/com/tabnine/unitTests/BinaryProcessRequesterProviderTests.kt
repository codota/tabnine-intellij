package com.tabnineCommon.unitTests

import com.tabnineCommon.binary.BinaryProcessGateway
import com.tabnineCommon.binary.BinaryProcessGatewayProvider
import com.tabnineCommon.binary.BinaryProcessRequesterProvider
import com.tabnineCommon.binary.BinaryRun
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.general.Utils
import com.tabnineCommon.testUtils.mockExponentialBackoffWith
import com.tabnineCommon.testUtils.mockThreadResponseWith
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.verification.VerificationMode
import java.io.IOException
import java.util.concurrent.Future

@ExtendWith(MockitoExtension::class)
class BinaryProcessRequesterProviderTests {
    @Mock
    private val binaryRun: BinaryRun? = null
    @Mock
    private val binaryProcessGatewayProvider: BinaryProcessGatewayProvider? = null
    @Mock
    private val binaryProcessGateway: BinaryProcessGateway? = null
    @Mock
    private val mockedFuture: Future<*>? = null

    private var binaryProcessRequesterProvider: BinaryProcessRequesterProvider? = null

    private var utilsMockedStatic: MockedStatic<Utils>? = null

    private val TIMES_OF_INIT_CALLS_ON_CREATION = 1
    private val TIMEOUT_THRESHOLD = 5000
    private val MOCKED_BACKOFF_TIME_MS = 5000

    @BeforeEach
    fun init() {
        utilsMockedStatic = mockThreadResponseWith(mockedFuture)
        Mockito.`when`(binaryProcessGatewayProvider!!.generateBinaryProcessGateway())
            .thenReturn(binaryProcessGateway)
    }

    @AfterEach
    fun cleanup() {
        utilsMockedStatic?.close()
    }

    @Test
    @Throws(IOException::class)
    fun shouldNotInitProcessWhenBinaryInitIsNull() {
        binaryProcessRequesterProvider = BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0)

        executeOnDead()

        Mockito.verify(binaryProcessGateway, timesBeyondCreation(0))?.init(ArgumentMatchers.any())
    }

    @Test
    @Throws(IOException::class)
    fun shouldNotInitProcessWhenBinaryInitIsNotDone() {
        Mockito.`when`(mockedFuture?.isDone).thenReturn(false)
        binaryProcessRequesterProvider = BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0)

        executeOnDead()

        Mockito.verify(binaryProcessGateway, timesBeyondCreation(0))?.init(ArgumentMatchers.any())
    }

    @Test
    @Throws(IOException::class)
    fun shouldInitProcessWhenBinaryInitIsDone() {
        Mockito.`when`(mockedFuture?.isDone).thenReturn(true)
        binaryProcessRequesterProvider = BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0)

        executeOnDead()

        Mockito.verify(binaryProcessGateway, timesBeyondCreation(1))?.init(ArgumentMatchers.any())
    }

    @Test
    @Throws(IOException::class)
    fun shouldInitProcessOnceOnMultipleCallsWithinBackoffTime() {
        val staticConfigMockedStatic = mockExponentialBackoffWith(MOCKED_BACKOFF_TIME_MS)
        Mockito.`when`(mockedFuture?.isDone).thenReturn(true)
        binaryProcessRequesterProvider = BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0)

        executeOnDead(10)
        staticConfigMockedStatic.close()

        Mockito.verify(binaryProcessGateway, timesBeyondCreation(1))?.init(ArgumentMatchers.any())
    }

    @Test
    @Throws(IOException::class)
    fun shouldInitProcessMultipleTimesOnMultipleCallsOutsideBackoffTime() {
        val staticConfigMockedStatic = mockExponentialBackoffWith(0)
        Mockito.`when`(mockedFuture?.isDone).thenReturn(true)
        binaryProcessRequesterProvider = BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0)

        executeOnDead(10)
        staticConfigMockedStatic.close()

        Mockito.verify(binaryProcessGateway, timesBeyondCreation(10))?.init(ArgumentMatchers.any())
    }

    @Test
    fun shouldResetRestartAttemptCounterOnSuccess() {
        Mockito.`when`(mockedFuture?.isDone).thenReturn(true)
        binaryProcessRequesterProvider = BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0)
        var staticConfigMockedStatic = mockExponentialBackoffWith(0)
        for (i in 0..9) {
            executeOnDead()
            staticConfigMockedStatic.verify(
                { StaticConfig.exponentialBackoff(Mockito.eq(i)) }, Mockito.times(1)
            )
        }
        staticConfigMockedStatic.close()
        executeOnSuccessful()

        staticConfigMockedStatic = mockExponentialBackoffWith(0)
        executeOnDead()

        staticConfigMockedStatic.verify({ StaticConfig.exponentialBackoff(Mockito.eq(0)) }, Mockito.times(1))
        staticConfigMockedStatic.close()
    }

    @Test
    @Throws(IOException::class)
    fun shouldInitProcessWhenElapsedSinceFirstTimeoutIsGreaterThanThreshold() {
        Mockito.`when`(mockedFuture?.isDone).thenReturn(true)
        binaryProcessRequesterProvider = BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0)

        executeOnTimeout()

        Mockito.verify(binaryProcessGateway, timesBeyondCreation(1))?.init(ArgumentMatchers.any())
    }

    @Test
    @Throws(IOException::class)
    fun shouldNotInitProcessWhenElapsedSinceFirstTimeoutIsLessThanThreshold() {
        binaryProcessRequesterProvider = BinaryProcessRequesterProvider.create(
            binaryRun, binaryProcessGatewayProvider, TIMEOUT_THRESHOLD
        )

        executeOnTimeout()

        Mockito.verify(binaryProcessGateway, timesBeyondCreation(0))?.init(ArgumentMatchers.any())
    }

    private fun executeOnSuccessful() {
        binaryProcessRequesterProvider?.onSuccessfulRequest()
    }

    private fun executeOnDead() {
        binaryProcessRequesterProvider?.onDead(Exception())
    }

    private fun executeOnTimeout() {
        binaryProcessRequesterProvider?.onTimeout()
    }

    private fun executeOnDead(times: Int) {
        for (i in 0 until times) {
            executeOnDead()
        }
    }

    private fun timesBeyondCreation(times: Int): VerificationMode {
        return Mockito.times(times + TIMES_OF_INIT_CALLS_ON_CREATION)
    }
}
