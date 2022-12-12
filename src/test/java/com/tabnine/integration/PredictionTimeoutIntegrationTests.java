package com.tabnine.integration;

import static com.tabnine.general.StaticConfig.COMPLETION_TIME_THRESHOLD;
import static com.tabnine.testUtils.MockAnswersUtilsKt.returnAfter;
import static com.tabnine.testUtils.MockAnswersUtilsKt.returnAfterTimeout;
import static com.tabnine.testUtils.TabnineMatchers.lookupBuilder;
import static com.tabnine.testUtils.TabnineMatchers.lookupElement;
import static com.tabnine.testUtils.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import com.intellij.codeInsight.lookup.LookupElement;
import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.binary.*;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.general.DependencyContainer;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.mockito.stubbing.Answer;

public class PredictionTimeoutIntegrationTests extends MockedBinaryCompletionTestCase {
  @Test
  public void givenAFileWhenCompletionFiredAndResponseTakeMoreThanThresholdThenResponseIsNulled()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).then(returnAfterTimeout(A_PREDICTION_RESULT));

    LookupElement[] actual = myFixture.completeBasic();

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void
      givenAFileWhenCompletionFiredAndResponseTakeMoreThanThresholdThenResponseIsNulledAndThePrecedingResponseGoThrough()
          throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .then(returnAfterTimeout(A_PREDICTION_RESULT))
        .thenReturn(A_PREDICTION_RESULT);

    LookupElement[] actual = myFixture.completeBasic();

    assertThat(actual, is(nullValue()));

    assertThat(
        myFixture.completeBasic(),
        array(
            lookupBuilder("hello"),
            lookupElement("return result"),
            lookupElement("return result;")));
  }

  @Test
  public void
      givenPreviousTimedOutCompletionWhenCompletionThenPreviousResultIsIgnoredAndCurrentIsReturned()
          throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .then(returnAfterTimeout(A_PREDICTION_RESULT))
        .then(returnAfter(SECOND_PREDICTION_RESULT, EPSILON));

    assertThat(myFixture.completeBasic(), nullValue());
    assertThat(myFixture.completeBasic(), array(lookupBuilder("hello"), lookupElement("test")));
  }

  @Test
  public void
      givenConsecutiveTimeOutsCompletionWhenCompletionThenPreviousResultIsIgnoredAndCurrentIsReturned()
          throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .then(returnAfterTimeout(A_PREDICTION_RESULT))
        .then(returnAfterTimeout(A_PREDICTION_RESULT))
        .then(returnAfter(SECOND_PREDICTION_RESULT, EPSILON));

    assertThat(myFixture.completeBasic(), nullValue());
    Thread.sleep(DependencyContainer.binaryRequestsTimeoutsThresholdMillis + EPSILON);
    assertThat(myFixture.completeBasic(), nullValue());
    verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
    assertThat(myFixture.completeBasic(), array(lookupBuilder("hello"), lookupElement("test")));
  }

  @Test
  public void givenCompletionTimeOutsButNotConsecutiveWhenCompletionThenRestartIsNotHappening()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .then(returnAfterTimeout(A_PREDICTION_RESULT))
        .thenReturn(A_PREDICTION_RESULT)
        .then(returnAfterTimeout(A_PREDICTION_RESULT))
        .thenReturn(SECOND_PREDICTION_RESULT);

    assertThat(myFixture.completeBasic(), nullValue());
    assertThat(myFixture.completeBasic(), notNullValue());
    assertThat(myFixture.completeBasic(), nullValue());

    assertThat(myFixture.completeBasic(), array(lookupBuilder("hello"), lookupElement("test")));
    verify(binaryProcessGatewayProviderMock, never()).generateBinaryProcessGateway();
  }

  @Test
  public void givenLateComingFailingRequestsWhenCompletionThenRestartIsHappeningOnlyOnce()
      throws Exception {
    AtomicInteger index = new AtomicInteger();
    when(binaryProcessGatewayMock.readRawResponse())
        .then(
            (invocation) -> {
              int currentIndex = index.getAndIncrement();

              if (currentIndex == 0) {
                Thread.sleep(2 * COMPLETION_TIME_THRESHOLD);

                throw new IOException();
              }

              Thread.sleep(EPSILON);

              throw new IOException();
            });

    assertThat(myFixture.completeBasic(), nullValue());
    assertThat(myFixture.completeBasic(), nullValue());

    Thread.sleep(3 * COMPLETION_TIME_THRESHOLD);
    verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
  }

  @Test
  public void pollerTimeoutExceptionThrown() throws Exception {
    BinaryProcessRequesterPollerCappedImpl binaryProcessRequesterPollerCapped =
        new BinaryProcessRequesterPollerCappedImpl(5, 10, 10);
    when(binaryProcessGatewayMock.readRawResponse()).thenThrow(new TabNineDeadException());
    boolean timeoutCalled = false;
    try {
      binaryProcessRequesterPollerCapped.pollUntilReady(binaryProcessGatewayMock);
    } catch (TabNineDeadException e) {
      timeoutCalled = true;
    }
    assertTrue(timeoutCalled);
  }

  @Test
  public void pollerHandlesTimeoutSuccesfully() throws Exception {
    BinaryProcessRequesterPollerCappedImpl binaryProcessRequesterPollerCapped =
        new BinaryProcessRequesterPollerCappedImpl(5, 10, 19);
    boolean timeoutCalled = false;

    final int[] pollCount = {0};
    when(binaryProcessGatewayMock.readRawResponse())
        .thenAnswer(
            (Answer<String>)
                invocation -> {
                  pollCount[0] = pollCount[0] + 1;
                  int count = pollCount[0];
                  if (count == 4) {
                    return SET_STATE_RESPONSE;
                  }
                  try {
                    Thread.sleep(20);
                  } catch (InterruptedException ignored) {

                  }
                  return null;
                });
    try {
      binaryProcessRequesterPollerCapped.pollUntilReady(binaryProcessGatewayMock);
    } catch (TabNineDeadException e) {
      timeoutCalled = true;
    }
    assertFalse(timeoutCalled);
    assertEquals(4, pollCount[0]);
  }
}
