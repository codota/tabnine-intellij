package com.tabnineCommon.integration;

import static com.tabnineCommon.general.StaticConfig.ILLEGAL_RESPONSE_THRESHOLD;
import static com.tabnineCommon.general.StaticConfig.sleepUponFailure;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import com.intellij.codeInsight.lookup.LookupElement;
import com.tabnineCommon.MockedBinaryCompletionTestCase;
import com.tabnineCommon.testUtils.TabnineMatchers;
import com.tabnineCommon.testUtils.TestData;
import com.tabnineCommon.binary.exceptions.TabNineDeadException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.stubbing.Answer;

public class BinaryBadResultsIntegrationTests extends MockedBinaryCompletionTestCase {

  @Before
  public void beforeEach() {
    myFixture.completeBasic(); // make sure all singletons are inited
    clearInvocations(binaryProcessGatewayProviderMock);
  }

  @Test
  public void givenACompletionWhenIOExceptionWasThrownThenBinaryIsRestarted() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenThrow(new IOException());

    LookupElement[] actual = myFixture.completeBasic();

    assertThat(actual, is(nullValue()));
    verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
  }

  @Test
  public void givenBinaryProcessFailsAtSomeSortThenOldProcessIsDestoryed() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenThrow(new IOException());

    myFixture.completeBasic();

    verify(binaryProcessGatewayMock).destroy();
  }

  @Test
  public void givenACompletionWhenBufferedReaderIsFinishedThenBinaryIsRestarted() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenThrow(new TabNineDeadException());

    LookupElement[] actual = myFixture.completeBasic();

    assertThat(actual, is(nullValue()));
    verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
  }

  @Test
  public void givenACompletionWhenBinaryReturnNonsenseThenNoResultReturned() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(TestData.INVALID_RESULT);

    LookupElement[] actual = myFixture.completeBasic();

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void givenConsecutiveCompletionsWhenBinaryReturnNonsenseThenBinaryIsRestarted()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(TestData.INVALID_RESULT);

    for (int i = 0; i < ILLEGAL_RESPONSE_THRESHOLD + TestData.OVERFLOW; i++) {
      myFixture.completeBasic();
    }

    verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
  }

  @Test
  public void givenConsecutiveCompletionsWhenBinaryReturnNullThenBinaryIsRestarted()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(TestData.NULL_RESULT);

    for (int i = 0; i < ILLEGAL_RESPONSE_THRESHOLD + TestData.OVERFLOW; i++) {
      myFixture.completeBasic();
    }

    verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
  }

  @Ignore(
      "This test works, but only when ran alone, as it is tied to the startup mechanism which fires only for the 1st test in the suite.")
  @Test
  public void
      givenBinaryIsFailingOnStartThenExtensionWillTryAgainAfterAWhileAndPredictionsWillNull()
          throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(TestData.A_PREDICTION_RESULT);
    doThrow(new IOException()).when(binaryProcessGatewayMock).init(any());

    assertThat(myFixture.completeBasic(), is(nullValue()));

    sleepUponFailure(1);

    verify(binaryProcessGatewayMock, times(2)).init(any());
  }

  @Test
  public void givenBinaryIsRestartedWhenNextCompletionFiresThenResponseIsValid() throws Exception {
    AtomicBoolean first = new AtomicBoolean(true);
    when(binaryProcessGatewayMock.readRawResponse())
        .thenAnswer(
            (Answer<String>)
                invocation -> {
                  if (first.get()) {
                    first.set(false);
                    throw new TabNineDeadException();
                  }

                  return TestData.A_PREDICTION_RESULT;
                });

    LookupElement[] actual = myFixture.completeBasic();

    assertThat(actual, is(nullValue()));
    verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();

    assertThat(
        myFixture.completeBasic(),
        array(
            TabnineMatchers.lookupBuilder("hello"),
            TabnineMatchers.lookupElement("return result"),
            TabnineMatchers.lookupElement("return result;")));
  }
}
