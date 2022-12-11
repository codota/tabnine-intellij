package com.tabnine.integration;

import static com.tabnine.testUtils.BadResultsUtils.enoughBadResultsToCauseADeath;
import static com.tabnine.testUtils.BadResultsUtils.overThresholdBadResultsWithAGoodResultInBetween;
import static com.tabnine.testUtils.TabnineMatchers.lookupBuilder;
import static com.tabnine.testUtils.TabnineMatchers.lookupElement;
import static com.tabnine.testUtils.TestData.*;
import static org.hamcrest.Matchers.array;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import com.intellij.codeInsight.completion.CompletionType;
import com.tabnine.MockedBinaryCompletionTestCase;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.junit.Test;

public class PredictionBehaviourIntegrationTests extends MockedBinaryCompletionTestCase {
  @Test
  public void givenAFileWhenCompletionFiredThenRequestIsWrittenToBinaryProcessInputCorrectly()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);

    myFixture.completeBasic();

    verify(binaryProcessGatewayMock).writeRequest(A_REQUEST_TO_TABNINE_BINARY);
  }

  @Test
  public void givenAFileWhenCompletionFiredThenResponseFromBinaryParsedCorrectlyToResults()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);

    assertThat(
        myFixture.completeBasic(),
        array(
            lookupBuilder("hello"),
            lookupElement("return result"),
            lookupElement("return result;")));
  }

  @Test
  public void givenInvalidCompletionsThatAreNotConsecutiveThanPluginIsNotDead() throws Exception {
    String[] enoughResultsToCauseDeathIfWerentForGoodResultBetweenEndingWithAGoodResult =
        Stream.concat(
                overThresholdBadResultsWithAGoodResultInBetween(), Stream.of(A_PREDICTION_RESULT))
            .toArray(String[]::new);
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(
            INVALID_RESULT,
            enoughResultsToCauseDeathIfWerentForGoodResultBetweenEndingWithAGoodResult);

    for (int i = 0;
        i < enoughResultsToCauseDeathIfWerentForGoodResultBetweenEndingWithAGoodResult.length;
        i++) {
      myFixture.completeBasic();
    }

    assertThat(
        myFixture.completeBasic(),
        array(
            lookupBuilder("hello"),
            lookupElement("return result"),
            lookupElement("return result;")));
  }

  @Test
  public void givenMoreConsecutiveInvalidCompletionsThanThresholdThenPluginDies() throws Exception {
    String[] badResults = enoughBadResultsToCauseADeath().toArray(String[]::new);
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(INVALID_RESULT, badResults);

    assertThrows(
        ExecutionException.class,
        () -> {
          for (int i = 0; i < badResults.length; i++) {
            myFixture.completeBasic();
          }
        });
  }

  @Test
  public void whenManuallyTriggeringCompletionThenEventIsFired() {
    myFixture.completeBasic();

    verify(completionEventSenderMock, times(1)).sendManualSuggestionTrigger();
  }

  @Test
  public void whenAutoTriggeringCompletionThenEventIsNotFired() {
    myFixture.complete(CompletionType.BASIC, 0);

    verify(completionEventSenderMock, never()).sendManualSuggestionTrigger();
  }
}
