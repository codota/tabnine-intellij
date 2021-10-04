package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import com.tabnine.binary.BinaryProcessRequesterPollerCappedImpl;
import com.tabnine.binary.exceptions.TabNineDeadException;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.tabnine.general.StaticConfig.COMPLETION_TIME_THRESHOLD;
import static com.tabnine.general.StaticConfig.CONSECUTIVE_TIMEOUTS_THRESHOLD;
import static com.tabnine.testutils.TabnineMatchers.lookupBuilder;
import static com.tabnine.testutils.TabnineMatchers.lookupElement;
import static com.tabnine.testutils.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class PredictionTimeoutIntegrationTests extends MockedBinaryCompletionTestCase {
    @Test
    public void givenAFileWhenCompletionFiredAndResponseTakeMoreThanThresholdThenResponseIsNulled() throws Exception {
        when(binaryProcessGatewayMock.readRawResponse()).thenAnswer(invocation -> {
            Thread.sleep(COMPLETION_TIME_THRESHOLD + OVERFLOW);

            return A_PREDICTION_RESULT;
        });

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void givenAFileWhenCompletionFiredAndResponseTakeMoreThanThresholdThenResponseIsNulledAndThePrecidingResponseGoThrough() throws Exception {
        AtomicBoolean first = new AtomicBoolean(true);
        when(binaryProcessGatewayMock.readRawResponse()).thenAnswer(invocation -> {
            if (first.get()) {
                first.set(false);
                Thread.sleep(COMPLETION_TIME_THRESHOLD + OVERFLOW);
            }

            return A_PREDICTION_RESULT;
        });

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("return result"),
                lookupElement("return result;")
        ));
    }

    @Test
    public void givenPreviousTimedOutCompletionWhenCompletionThenPreviousResultIsIgnoredAndCurrentIsReturned() throws Exception {
        AtomicInteger index = new AtomicInteger();
        when(binaryProcessGatewayMock.readRawResponse()).then((invocation) -> {
            if (index.getAndIncrement() == 0) {
                Thread.sleep(COMPLETION_TIME_THRESHOLD + EPSILON);

                return A_PREDICTION_RESULT;
            }

            Thread.sleep(EPSILON);
            return SECOND_PREDICTION_RESULT;
        });

        assertThat(myFixture.completeBasic(), nullValue());
        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("test")
        ));
    }

    @Test
    public void givenConsecutivesTimeOutsCompletionWhenCompletionThenPreviousResultIsIgnoredAndCurrentIsReturned() throws Exception {
        AtomicInteger index = new AtomicInteger();
        when(binaryProcessGatewayMock.readRawResponse()).then((invocation) -> {
            if (index.getAndIncrement() < CONSECUTIVE_TIMEOUTS_THRESHOLD) {
                Thread.sleep(COMPLETION_TIME_THRESHOLD + EPSILON);

                return A_PREDICTION_RESULT;
            }

            Thread.sleep(EPSILON);
            return SECOND_PREDICTION_RESULT;
        });

        for (int i = 0; i < CONSECUTIVE_TIMEOUTS_THRESHOLD; i++) {
            LookupElement[] lookupElements = myFixture.completeBasic();
            String val = "N/A";
            if (lookupElements != null) {
                val = Arrays.stream(lookupElements).map(LookupElement::getLookupString).collect(Collectors.joining(", "));
            }
            assertThat(val + " is not null in the " + (i+1) + " attempt", lookupElements, nullValue());
        }

        verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("test")
        ));
    }

    @Test
    public void givenCompletionTimeOutsButNotConsecutiveWhenCompletionThenRestartIsNotHappening() throws Exception {
        AtomicInteger index = new AtomicInteger();
        when(binaryProcessGatewayMock.readRawResponse()).then((invocation) -> {
            int currentIndex = index.getAndIncrement();

            if (currentIndex == INDEX_OF_SOME_VALID_RESULT_BETWEEN_TIMEOUTS) {
                return A_PREDICTION_RESULT;
            }

            if (currentIndex < CONSECUTIVE_TIMEOUTS_THRESHOLD + OVERFLOW) {
                Thread.sleep(COMPLETION_TIME_THRESHOLD + EPSILON);

                return A_PREDICTION_RESULT;
            }

            Thread.sleep(EPSILON);
            return SECOND_PREDICTION_RESULT;
        });

        for (int i = 0; i < CONSECUTIVE_TIMEOUTS_THRESHOLD + OVERFLOW; i++) {
            if (i != INDEX_OF_SOME_VALID_RESULT_BETWEEN_TIMEOUTS) {
                assertThat(myFixture.completeBasic(), nullValue());
            } else {
                myFixture.completeBasic();
            }
        }

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("test")
        ));
        verify(binaryProcessGatewayProviderMock, never()).generateBinaryProcessGateway();
    }

    @Test
    public void givenLateComingFailingRequestsWhenCompletionThenRestartIsHappeningOnlyOnce() throws Exception {
        AtomicInteger index = new AtomicInteger();
        when(binaryProcessGatewayMock.readRawResponse()).then((invocation) -> {
            int currentIndex = index.getAndIncrement();

            if (currentIndex == 0) {
                Thread.sleep(2 * COMPLETION_TIME_THRESHOLD);

                throw new IOException();
            }

            Thread.sleep(EPSILON);

            throw new IOException();
        });

        assertThat(myFixture.completeBasic(), nullValue());
        assertThat(myFixture.completeBasic(), nullValue());

        Thread.sleep(3 * COMPLETION_TIME_THRESHOLD);
        verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
    }

    @Test
    public void pollerTimeoutExceptionThrown() throws Exception {
        BinaryProcessRequesterPollerCappedImpl binaryProcessRequesterPollerCapped = new BinaryProcessRequesterPollerCappedImpl(5, 10, 10);
        when(binaryProcessGatewayMock.readRawResponse()).thenThrow(new TabNineDeadException());
        boolean timeoutCalled = false;
        try {
            binaryProcessRequesterPollerCapped.pollUntilReady(binaryProcessGatewayMock);
        } catch (TabNineDeadException e) {
            timeoutCalled = true;
        }
        assertTrue(timeoutCalled);
    }

    @Test
    public void pollerHandlesTimeoutSuccesfully() throws Exception {
        BinaryProcessRequesterPollerCappedImpl binaryProcessRequesterPollerCapped = new BinaryProcessRequesterPollerCappedImpl(5, 10, 19);
        boolean timeoutCalled = false;

        final int[] pollCount = {0};
        when(binaryProcessGatewayMock.readRawResponse()).thenAnswer((Answer<String>) invocation -> {
            pollCount[0] = pollCount[0] + 1;
            int count = pollCount[0];
            if (count == 4) {
                return SET_STATE_RESPONSE;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {

            }
            return null;
        });
        try {
            binaryProcessRequesterPollerCapped.pollUntilReady(binaryProcessGatewayMock);
        } catch (TabNineDeadException e) {
            timeoutCalled = true;
        }
        assertFalse(timeoutCalled);
        assertEquals(4, pollCount[0]);
    }
}
