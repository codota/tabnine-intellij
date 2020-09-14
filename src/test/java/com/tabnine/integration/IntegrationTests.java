package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import com.tabnine.TabNineProcessFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import static com.tabnine.TabNineCompletionContributor.COMPLETION_THRESHOLD;
import static com.tabnine.integration.TabnineMatchers.lookupBuilder;
import static com.tabnine.integration.TabnineMatchers.lookupElement;
import static com.tabnine.integration.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class IntegrationTests extends LightPlatformCodeInsightFixture4TestCase {
    private Process processMock;
    private BufferedReader readerMock;
    private OutputStream outputStreamMock;

    @Before
    public void initChildProcessMock() {
        processMock = Mockito.mock(Process.class);
        readerMock = Mockito.mock(BufferedReader.class);
        outputStreamMock = Mockito.mock(OutputStream.class);

        TabNineProcessFactory.setProcessForTesting(processMock, readerMock);
        when(processMock.getOutputStream()).thenReturn(outputStreamMock);
        when(processMock.isAlive()).thenReturn(true);
    }

    @Test
    public void givenAFileWhenCompletionFiredThenRequestIsWrittenToBinaryProcessInputCorrectly() throws IOException {
        myFixture.configureByText(A_TEST_TXT_FILE, SOME_CONTENT);

        myFixture.completeBasic();

        verify(outputStreamMock).write(A_REQUEST_TO_TABNINE_BINARY);
    }

    @Test
    public void givenAFileWhenCompletionFiredThenResponseFromBinaryParsedCorrectlyToResults() throws IOException {
        myFixture.configureByText(A_TEST_TXT_FILE, SOME_CONTENT);
        when(readerMock.readLine()).thenReturn(A_PREDICTION_RESULT);

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("\\n return result"),
                lookupElement("\\n return result;\\n")
        ));
    }

    @Test
    public void givenAFileWhenCompletionFiredAndResponseTakeMoreThanThresholdThenResponseIsNulled() throws IOException {
        myFixture.configureByText(A_TEST_TXT_FILE, SOME_CONTENT);
        when(readerMock.readLine()).thenAnswer(invocation -> {
            Thread.sleep(COMPLETION_THRESHOLD + OVERFLOW);

            return A_PREDICTION_RESULT;
        });

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void givenACompletionWhenIOExceptionWasThrownThanBinaryIsRestarted() throws IOException {
        myFixture.configureByText(A_TEST_TXT_FILE, SOME_CONTENT);
        when(readerMock.readLine()).thenThrow(new IOException());

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
        verify(processMock).destroy();
    }

    @Test
    public void givenACompletionWhenBufferedReaderIsFinishedThanBinaryIsRestarted() throws IOException {
        myFixture.configureByText(A_TEST_TXT_FILE, SOME_CONTENT);
        when(readerMock.readLine()).thenReturn(null);

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
        verify(processMock).destroy();
    }

    @Test
    public void givenACompletionWhenBinaryReturnNonsenseThanBinaryIsRestarted() throws IOException {
        myFixture.configureByText(A_TEST_TXT_FILE, SOME_CONTENT);
        when(readerMock.readLine()).thenReturn("Nonsense");

        LookupElement[] actual = myFixture.completeBasic();

        assertThat(actual, is(nullValue()));
        verify(processMock).destroy();
    }
}