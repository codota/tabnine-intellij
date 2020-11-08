package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
        when(tabNineBinaryMock.readRawResponse()).thenAnswer(invocation -> {
            Thread.sleep(COMPLETION_TIME_THRESHOLD + OVERFLOW);

            return A_PREDICTION_RESULT;
        });

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void givenAFileWhenCompletionFiredAndResponseTakeMoreThanThresholdThenResponseIsNulledAndThePrecidingResponseGoThrough() throws Exception {
        AtomicBoolean first = new AtomicBoolean(true);
        when(tabNineBinaryMock.readRawResponse()).thenAnswer(invocation -> {
            if(first.get()) {
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
        when(tabNineBinaryMock.readRawResponse()).then((invocation) -> {
            if(index.getAndIncrement() == 0) {
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
        when(tabNineBinaryMock.readRawResponse()).then((invocation) -> {
            if(index.getAndIncrement() < CONSECUTIVE_TIMEOUTS_THRESHOLD) {
                Thread.sleep(COMPLETION_TIME_THRESHOLD + EPSILON);

                return A_PREDICTION_RESULT;
            }

            Thread.sleep(EPSILON);
            return SECOND_PREDICTION_RESULT;
        });

        for(int i = 0; i < CONSECUTIVE_TIMEOUTS_THRESHOLD; i++) {
            assertThat(myFixture.completeBasic(), nullValue());
        }

        verify(tabNineBinaryMock).restart();
        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("test")
        ));
    }

    @Test
    public void givenCompletionTimeOutsButNotConsecutiveWhenCompletionThenRestartIsNotHappening() throws Exception {
        AtomicInteger index = new AtomicInteger();
        when(tabNineBinaryMock.readRawResponse()).then((invocation) -> {
            int currentIndex = index.getAndIncrement();

            if(currentIndex == INDEX_OF_SOME_VALID_RESULT_BETWEEN_TIMEOUTS) {
                return A_PREDICTION_RESULT;
            }

            if(currentIndex < CONSECUTIVE_TIMEOUTS_THRESHOLD + OVERFLOW) {
                Thread.sleep(COMPLETION_TIME_THRESHOLD + EPSILON);

                return A_PREDICTION_RESULT;
            }

            Thread.sleep(EPSILON);
            return SECOND_PREDICTION_RESULT;
        });

        for(int i = 0; i < CONSECUTIVE_TIMEOUTS_THRESHOLD + OVERFLOW; i++) {
            if(i != INDEX_OF_SOME_VALID_RESULT_BETWEEN_TIMEOUTS) {
                assertThat(myFixture.completeBasic(), nullValue());
            } else {
                myFixture.completeBasic();
            }
        }

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("test")
        ));
        verify(tabNineBinaryMock, never()).restart();
    }
}
