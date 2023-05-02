package com.tabnine.integration;

import static org.hamcrest.Matchers.array;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import com.intellij.codeInsight.completion.CompletionType;
import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.capabilities.RenderingMode;
import com.tabnine.testUtils.TabnineMatchers;
import com.tabnine.testUtils.TestData;
import org.junit.Test;

public class PredictionBehaviourIntegrationTests extends MockedBinaryCompletionTestCase {
  @Test
  public void givenAFileWhenCompletionFiredThenRequestIsWrittenToBinaryProcessInputCorrectly()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(TestData.A_PREDICTION_RESULT);

    myFixture.completeBasic();

    verify(binaryProcessGatewayMock).writeRequest(TestData.A_REQUEST_TO_TABNINE_BINARY);
  }

  @Test
  public void givenAFileWhenCompletionFiredThenResponseFromBinaryParsedCorrectlyToResults()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(TestData.A_PREDICTION_RESULT);

    assertThat(
        myFixture.completeBasic(),
        array(
            TabnineMatchers.lookupBuilder("hello"),
            TabnineMatchers.lookupElement("return result"),
            TabnineMatchers.lookupElement("return result;")));
  }

  @Test
  public void whenManuallyTriggeringCompletionThenEventIsFired() {
    myFixture.completeBasic();

    verify(completionEventSenderMock, times(1))
        .sendManualSuggestionTrigger(RenderingMode.AUTOCOMPLETE);
  }

  @Test
  public void whenAutoTriggeringCompletionThenEventIsNotFired() {
    myFixture.complete(CompletionType.BASIC, 0);

    verify(completionEventSenderMock, never())
        .sendManualSuggestionTrigger(RenderingMode.AUTOCOMPLETE);
  }
}
