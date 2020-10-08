package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.tabnine.general.StaticConfig.COMPLETION_TIME_THRESHOLD;
import static com.tabnine.testutils.TabnineMatchers.lookupBuilder;
import static com.tabnine.testutils.TabnineMatchers.lookupElement;
import static com.tabnine.testutils.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

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
        when(tabNineBinaryMock.getAndIncrementCorrelationId()).thenReturn(1);
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
        when(tabNineBinaryMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT, SECOND_PREDICTION_RESULT);
        when(tabNineBinaryMock.getAndIncrementCorrelationId()).thenReturn(2);

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("test")
        ));
    }
}
