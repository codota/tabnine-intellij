package com.tabnineCommon.integration;

import static com.tabnineCommon.general.StaticConfig.COMPLETION_TIME_THRESHOLD;
import static com.tabnineCommon.testUtils.MockAnswersUtilsKt.returnAfter;
import static com.tabnineCommon.testUtils.MockAnswersUtilsKt.returnAfterTimeout;
import static com.tabnineCommon.testUtils.TabnineMatchers.lookupBuilder;
import static com.tabnineCommon.testUtils.TabnineMatchers.lookupElement;
import static com.tabnineCommon.testUtils.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import com.intellij.codeInsight.lookup.LookupElement;
import com.tabnineCommon.MockedBinaryCompletionTestCase;
import com.tabnineCommon.general.DependencyContainer;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

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
}
