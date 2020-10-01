package com.tabnine.integration;

import org.junit.Test;

import static com.tabnine.testutils.TabnineMatchers.lookupBuilder;
import static com.tabnine.testutils.TabnineMatchers.lookupElement;
import static com.tabnine.testutils.TestData.A_PREDICTION_RESULT;
import static com.tabnine.testutils.TestData.A_REQUEST_TO_TABNINE_BINARY;
import static org.hamcrest.Matchers.array;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PredictionBehaviourIntegrationTests extends MockedBinaryCompletionTestCase {
    @Test
    public void givenAFileWhenCompletionFiredThenRequestIsWrittenToBinaryProcessInputCorrectly() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);
        when(tabNineBinaryMock.getAndIncrementCorrelationId()).thenReturn(1);

        myFixture.completeBasic();

        verify(tabNineBinaryMock).writeRequest(A_REQUEST_TO_TABNINE_BINARY);
    }

    @Test
    public void givenAFileWhenCompletionFiredThenResponseFromBinaryParsedCorrectlyToResults() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);
        when(tabNineBinaryMock.getAndIncrementCorrelationId()).thenReturn(1);

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("\\n return result"),
                lookupElement("\\n return result;\\n")
        ));
    }
}