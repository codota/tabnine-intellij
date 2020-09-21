package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import org.junit.Test;

import static com.tabnine.StaticConfig.COMPLETION_TIME_THRESHOLD;
import static com.tabnine.integration.TabnineMatchers.lookupBuilder;
import static com.tabnine.integration.TabnineMatchers.lookupElement;
import static com.tabnine.integration.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class PredictionTimeoutIntegrationTests extends MockedBinaryCompletionTestCase {
    @Test
    public void givenAFileWhenCompletionFiredAndResponseTakeMoreThanThresholdThenResponseIsNulled() throws Exception {
        when(tabNineFacadeMock.readRawResponse()).thenAnswer(invocation -> {
            Thread.sleep(COMPLETION_TIME_THRESHOLD + OVERFLOW);

            return A_PREDICTION_RESULT;
        });

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void givenPreviousTimedOutCompletionWhenCompletionThenPreviousResultIsIgnoredAndCurrentIsReturned() throws Exception {
        when(tabNineFacadeMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT, SECOND_PREDICTION_RESULT);
        when(tabNineFacadeMock.getAndIncrementCorrelationId()).thenReturn(2);

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("test")
        ));
    }
}
