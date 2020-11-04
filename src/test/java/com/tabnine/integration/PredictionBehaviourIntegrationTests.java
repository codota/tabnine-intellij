package com.tabnine.integration;

import com.tabnine.testutils.BadResultsUtils;
import org.junit.Test;

import java.util.stream.Stream;

import static com.tabnine.testutils.BadResultsUtils.overThresholdBadResultsWithAGoodResultInBetween;
import static com.tabnine.testutils.TabnineMatchers.lookupBuilder;
import static com.tabnine.testutils.TabnineMatchers.lookupElement;
import static com.tabnine.testutils.TestData.*;
import static org.hamcrest.Matchers.array;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class PredictionBehaviourIntegrationTests extends MockedBinaryCompletionTestCase {
    @Test
    public void givenAFileWhenCompletionFiredThenRequestIsWrittenToBinaryProcessInputCorrectly() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);

        myFixture.completeBasic();

        verify(tabNineBinaryMock).writeRequest(A_REQUEST_TO_TABNINE_BINARY);
    }

    @Test
    public void givenAFileWhenCompletionFiredThenResponseFromBinaryParsedCorrectlyToResults() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("return result"),
                lookupElement("return result;")
        ));
    }

    @Test
    public void givenConsecutiveCompletionsWhenBinaryReturnNullThanBinaryIsRestarted() throws Exception {
        String[] enoughResultsToCauseDeathIfWerentForGoodResultBetweenEndingWithAGoodResult = Stream.concat(overThresholdBadResultsWithAGoodResultInBetween(), Stream.of(A_PREDICTION_RESULT)).toArray(String[]::new);
        when(tabNineBinaryMock.readRawResponse()).thenReturn(INVALID_RESULT, enoughResultsToCauseDeathIfWerentForGoodResultBetweenEndingWithAGoodResult);

        for (int i = 0; i < enoughResultsToCauseDeathIfWerentForGoodResultBetweenEndingWithAGoodResult.length; i++) {
            myFixture.completeBasic();
        }

        assertThat(myFixture.completeBasic(), array(
                lookupBuilder("hello"),
                lookupElement("return result"),
                lookupElement("return result;")
        ));
    }
}