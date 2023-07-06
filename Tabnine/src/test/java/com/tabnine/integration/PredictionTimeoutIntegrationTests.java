package com.tabnine.integration;

import static com.tabnine.testUtils.MockAnswersUtilsKt.returnAfter;
import static com.tabnine.testUtils.MockAnswersUtilsKt.returnAfterTimeout;
import static com.tabnineCommon.general.StaticConfig.COMPLETION_TIME_THRESHOLD;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import com.intellij.codeInsight.lookup.LookupElement;
import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.testUtils.TabnineMatchers;
import com.tabnine.testUtils.TestData;
import com.tabnineCommon.general.DependencyContainer;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PredictionTimeoutIntegrationTests extends MockedBinaryCompletionTestCase {
  @Test
  public void givenAFileWhenCompletionFiredAndResponseTakeMoreThanThresholdThenResponseIsNulled()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .then(returnAfterTimeout(TestData.A_PREDICTION_RESULT));

    LookupElement[] actual = myFixture.completeBasic();

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void
      givenAFileWhenCompletionFiredAndResponseTakeMoreThanThresholdThenResponseIsNulledAndThePrecedingResponseGoThrough()
          throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .then(returnAfterTimeout(TestData.A_PREDICTION_RESULT))
        .thenReturn(TestData.A_PREDICTION_RESULT);

    LookupElement[] actual = myFixture.completeBasic();

    assertThat(actual, is(nullValue()));

    assertThat(
        myFixture.completeBasic(),
        array(
            TabnineMatchers.lookupBuilder("hello"),
            TabnineMatchers.lookupElement("return result"),
            TabnineMatchers.lookupElement("return result;")));
  }

  @Test
  public void
      givenPreviousTimedOutCompletionWhenCompletionThenPreviousResultIsIgnoredAndCurrentIsReturned()
          throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .then(returnAfterTimeout(TestData.A_PREDICTION_RESULT))
        .then(returnAfter(TestData.SECOND_PREDICTION_RESULT, TestData.EPSILON));

    assertThat(myFixture.completeBasic(), nullValue());
    assertThat(
        myFixture.completeBasic(),
        array(TabnineMatchers.lookupBuilder("hello"), TabnineMatchers.lookupElement("test")));
  }

  @Test
  public void
      givenConsecutiveTimeOutsCompletionWhenCompletionThenPreviousResultIsIgnoredAndCurrentIsReturned()
          throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .then(returnAfterTimeout(TestData.A_PREDICTION_RESULT))
        .then(returnAfterTimeout(TestData.A_PREDICTION_RESULT))
        .then(returnAfter(TestData.SECOND_PREDICTION_RESULT, TestData.EPSILON));

    assertThat(myFixture.completeBasic(), nullValue());
    Thread.sleep(DependencyContainer.binaryRequestsTimeoutsThresholdMillis + TestData.EPSILON);
    assertThat(myFixture.completeBasic(), nullValue());
    verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
    assertThat(
        myFixture.completeBasic(),
        array(TabnineMatchers.lookupBuilder("hello"), TabnineMatchers.lookupElement("test")));
  }

  @Test
  public void givenCompletionTimeOutsButNotConsecutiveWhenCompletionThenRestartIsNotHappening()
      throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .then(returnAfterTimeout(TestData.A_PREDICTION_RESULT))
        .thenReturn(TestData.A_PREDICTION_RESULT)
        .then(returnAfterTimeout(TestData.A_PREDICTION_RESULT))
        .thenReturn(TestData.SECOND_PREDICTION_RESULT);

    assertThat(myFixture.completeBasic(), nullValue());
    assertThat(myFixture.completeBasic(), notNullValue());
    assertThat(myFixture.completeBasic(), nullValue());

    assertThat(
        myFixture.completeBasic(),
        array(TabnineMatchers.lookupBuilder("hello"), TabnineMatchers.lookupElement("test")));
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

              Thread.sleep(TestData.EPSILON);

              throw new IOException();
            });

    assertThat(myFixture.completeBasic(), nullValue());
    assertThat(myFixture.completeBasic(), nullValue());

    Thread.sleep(3 * COMPLETION_TIME_THRESHOLD);
    verify(binaryProcessGatewayProviderMock).generateBinaryProcessGateway();
  }
}
