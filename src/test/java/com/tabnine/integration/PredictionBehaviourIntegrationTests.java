package com.tabnine.integration;

import org.junit.Test;

import static com.tabnine.integration.TabnineMatchers.lookupBuilder;
import static com.tabnine.integration.TabnineMatchers.lookupElement;
import static com.tabnine.integration.TestData.*;
import static org.hamcrest.Matchers.array;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PredictionBehaviourIntegrationTests extends MockedBinaryCompletionTestCase {
    @Test
    public void givenAFileWhenCompletionFiredThenRequestIsWrittenToBinaryProcessInputCorrectly() throws Exception {
        when(tabNineFacadeMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);
        when(tabNineFacadeMock.getAndIncrementCorrelationId()).thenReturn(1);

        myFixture.completeBasic();

        verify(tabNineFacadeMock).writeRequest(A_REQUEST_TO_TABNINE_BINARY);
    }

    @Test
    public void givenAFileWhenCompletionFiredThenResponseFromBinaryParsedCorrectlyToResults() throws Exception {
        when(tabNineFacadeMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);
        when(tabNineFacadeMock.getAndIncrementCorrelationId()).thenReturn(1);

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("\\n return result"),
                lookupElement("\\n return result;\\n")
        ));
    }
}