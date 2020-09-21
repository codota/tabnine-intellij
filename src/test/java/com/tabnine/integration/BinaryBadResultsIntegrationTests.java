package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import com.tabnine.exceptions.TabNineDeadException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.tabnine.StaticConfig.ILLEGAL_RESPONSE_THRESHOLD;
import static com.tabnine.StaticConfig.sleepUponFailure;
import static com.tabnine.integration.TestData.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BinaryBadResultsIntegrationTests extends MockedBinaryCompletionTestCase {
    @Test
    public void givenACompletionWhenIOExceptionWasThrownThanBinaryIsRestarted() throws Exception {
        when(tabNineFacadeMock.readRawResponse()).thenThrow(new IOException());

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
        verify(tabNineFacadeMock).restart();
    }

    @Test
    public void givenACompletionWhenBufferedReaderIsFinishedThanBinaryIsRestarted() throws Exception {
        when(tabNineFacadeMock.readRawResponse()).thenThrow(new TabNineDeadException());

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
        verify(tabNineFacadeMock).restart();
    }

    @Test
    public void givenACompletionWhenBinaryReturnNonsenseThanNoResultReturned() throws Exception {
        when(tabNineFacadeMock.readRawResponse()).thenReturn(INVALID_RESULT);

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void givenConsecutiveCompletionsWhenBinaryReturnNonsenseThanBinaryIsRestarted() throws Exception {
        when(tabNineFacadeMock.readRawResponse()).thenReturn(INVALID_RESULT);

        for(int i = 0; i < ILLEGAL_RESPONSE_THRESHOLD + OVERFLOW; i++) {
            myFixture.completeBasic();
        }

        verify(tabNineFacadeMock).restart();
    }

    @Ignore("This test works, but only when ran alone, as it is tied to the startup mechanism which fires only for the 1st test in the suite.")
    @Test
    public void givenBinaryIsFailingOnStartThenExtensionWillTryAgainAfterAWhileAndPredictionsWillNull() throws Exception {
        when(tabNineFacadeMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);
        doThrow(new IOException()).when(tabNineFacadeMock).create();

        assertThat(myFixture.completeBasic(), is(nullValue()));

        sleepUponFailure(2);

        verify(tabNineFacadeMock, times(2)).create();
    }
}
