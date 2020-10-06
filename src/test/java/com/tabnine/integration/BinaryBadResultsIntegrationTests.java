package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import com.tabnine.exceptions.TabNineDeadException;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tabnine.StaticConfig.ILLEGAL_RESPONSE_THRESHOLD;
import static com.tabnine.StaticConfig.sleepUponFailure;
import static com.tabnine.testutils.TabnineMatchers.lookupBuilder;
import static com.tabnine.testutils.TabnineMatchers.lookupElement;
import static com.tabnine.testutils.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BinaryBadResultsIntegrationTests extends MockedBinaryCompletionTestCase {
    @Test
    public void givenACompletionWhenIOExceptionWasThrownThanBinaryIsRestarted() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenThrow(new IOException());

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
        verify(tabNineBinaryMock).restart();
    }

    @Test
    public void givenACompletionWhenBufferedReaderIsFinishedThanBinaryIsRestarted() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenThrow(new TabNineDeadException());

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
        verify(tabNineBinaryMock).restart();
    }

    @Test
    public void givenACompletionWhenBinaryReturnNonsenseThanNoResultReturned() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(INVALID_RESULT);

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void givenConsecutiveCompletionsWhenBinaryReturnNonsenseThanBinaryIsRestarted() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(INVALID_RESULT);

        for (int i = 0; i < ILLEGAL_RESPONSE_THRESHOLD + OVERFLOW; i++) {
            myFixture.completeBasic();
        }

        verify(tabNineBinaryMock).restart();
    }

    @Test
    public void givenConsecutiveCompletionsWhenBinaryReturnNullThanBinaryIsRestarted() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(NULL_RESULT);

        for (int i = 0; i < ILLEGAL_RESPONSE_THRESHOLD + OVERFLOW; i++) {
            myFixture.completeBasic();
        }

        verify(tabNineBinaryMock).restart();
    }

    @Ignore("This test works, but only when ran alone, as it is tied to the startup mechanism which fires only for the 1st test in the suite.")
    @Test
    public void givenBinaryIsFailingOnStartThenExtensionWillTryAgainAfterAWhileAndPredictionsWillNull() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);
        doThrow(new IOException()).when(tabNineBinaryMock).create();

        assertThat(myFixture.completeBasic(), is(nullValue()));

        sleepUponFailure(1);

        verify(tabNineBinaryMock, times(2)).create();
    }

    @Test
    public void givenBinaryIsRestartedDueToTabNineDeadExceptionWhenNextCompletionFiresThenResponseIsValid() throws Exception {
        when(tabNineBinaryMock.getAndIncrementCorrelationId()).thenReturn(1);
        AtomicBoolean first = new AtomicBoolean(true);
        when(tabNineBinaryMock.readRawResponse()).thenAnswer((Answer<String>) invocation -> {
            if (first.get()) {
                first.set(false);
                throw new TabNineDeadException();
            }

            return A_PREDICTION_RESULT;
        });

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
        verify(tabNineBinaryMock).restart();

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("\\n return result"),
                lookupElement("\\n return result;\\n")
        ));
    }
}
