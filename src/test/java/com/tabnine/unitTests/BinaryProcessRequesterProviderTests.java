package com.tabnine.unitTests;

import static com.tabnine.testUtils.MockStaticMethodsUtilsKt.mockExponentialBackoffWith;
import static com.tabnine.testUtils.MockStaticMethodsUtilsKt.mockThreadResponseWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tabnine.binary.BinaryProcessGateway;
import com.tabnine.binary.BinaryProcessGatewayProvider;
import com.tabnine.binary.BinaryProcessRequesterProvider;
import com.tabnine.binary.BinaryRun;
import com.tabnine.general.StaticConfig;
import com.tabnine.general.Utils;
import java.io.IOException;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

@ExtendWith(MockitoExtension.class)
public class BinaryProcessRequesterProviderTests {
  @Mock private BinaryRun binaryRun;
  @Mock private BinaryProcessGatewayProvider binaryProcessGatewayProvider;
  @Mock private BinaryProcessGateway binaryProcessGateway;
  @Mock private Future<?> mockedFuture;
  private MockedStatic<Utils> utilsMockedStatic;
  private BinaryProcessRequesterProvider binaryProcessRequesterProvider;
  private final int TIMES_OF_INIT_CALLS_ON_CREATION = 1;
  private final int TIMEOUT_THRESHOLD = 5000;
  private final int MOCKED_BACKOFF_TIME_MS = 5000;

  @BeforeEach
  public void init() {
    utilsMockedStatic = mockThreadResponseWith(mockedFuture);
    when(binaryProcessGatewayProvider.generateBinaryProcessGateway())
        .thenReturn(binaryProcessGateway);
  }

  @AfterEach
  public void cleanup() {
    utilsMockedStatic.close();
  }

  @Test
  public void shouldNotInitProcessWhenBinaryInitIsNull() throws IOException {
    binaryProcessRequesterProvider =
        BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0);

    executeOnDead();

    verify(binaryProcessGateway, timesBeyondCreation(0)).init(any());
  }

  @Test
  public void shouldNotInitProcessWhenBinaryInitIsNotDone() throws IOException {
    when(mockedFuture.isDone()).thenReturn(false);
    binaryProcessRequesterProvider =
        BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0);

    executeOnDead();

    verify(binaryProcessGateway, timesBeyondCreation(0)).init(any());
  }

  @Test
  public void shouldInitProcessWhenBinaryInitIsDone() throws IOException {
    when(mockedFuture.isDone()).thenReturn(true);
    binaryProcessRequesterProvider =
        BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0);

    executeOnDead();

    verify(binaryProcessGateway, timesBeyondCreation(1)).init(any());
  }

  @Test
  public void shouldInitProcessOnceOnMultipleCallsWithinBackoffTime() throws IOException {
    MockedStatic<StaticConfig> staticConfigMockedStatic =
        mockExponentialBackoffWith(MOCKED_BACKOFF_TIME_MS);
    when(mockedFuture.isDone()).thenReturn(true);
    binaryProcessRequesterProvider =
        BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0);

    executeOnDead(10);
    staticConfigMockedStatic.close();

    verify(binaryProcessGateway, timesBeyondCreation(1)).init(any());
  }

  @Test
  public void shouldInitProcessMultipleTimesOnMultipleCallsOutsideBackoffTime() throws IOException {
    MockedStatic<StaticConfig> staticConfigMockedStatic = mockExponentialBackoffWith(0);
    when(mockedFuture.isDone()).thenReturn(true);
    binaryProcessRequesterProvider =
        BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0);

    executeOnDead(10);
    staticConfigMockedStatic.close();

    verify(binaryProcessGateway, timesBeyondCreation(10)).init(any());
  }

  @Test
  public void shouldResetRestartAttemptCounterOnSuccess() {
    when(mockedFuture.isDone()).thenReturn(true);
    binaryProcessRequesterProvider =
        BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0);

    MockedStatic<StaticConfig> staticConfigMockedStatic = mockExponentialBackoffWith(0);
    for (int i = 0; i < 10; i++) {
      executeOnDead();
      int restartAttemptCounter = i;
      staticConfigMockedStatic.verify(
          () -> StaticConfig.exponentialBackoff(Mockito.eq(restartAttemptCounter)), times(1));
    }
    staticConfigMockedStatic.close();

    executeOnSuccessful();

    staticConfigMockedStatic = mockExponentialBackoffWith(0);

    executeOnDead();

    staticConfigMockedStatic.verify(() -> StaticConfig.exponentialBackoff(Mockito.eq(0)), times(1));
    staticConfigMockedStatic.close();
  }

  @Test
  public void shouldInitProcessWhenElapsedSinceFirstTimeoutIsGreaterThanThreshold()
      throws IOException {
    when(mockedFuture.isDone()).thenReturn(true);
    binaryProcessRequesterProvider =
        BinaryProcessRequesterProvider.create(binaryRun, binaryProcessGatewayProvider, 0);
    executeOnTimeout();

    verify(binaryProcessGateway, timesBeyondCreation(1)).init(any());
  }

  @Test
  public void shouldNotInitProcessWhenElapsedSinceFirstTimeoutIsLessThanThreshold()
      throws IOException {
    binaryProcessRequesterProvider =
        BinaryProcessRequesterProvider.create(
            binaryRun, binaryProcessGatewayProvider, TIMEOUT_THRESHOLD);
    executeOnTimeout();

    verify(binaryProcessGateway, timesBeyondCreation(0)).init(any());
  }

  private void executeOnSuccessful() {
    binaryProcessRequesterProvider.onSuccessfulRequest();
  }

  private void executeOnDead() {
    binaryProcessRequesterProvider.onDead(new Exception());
  }

  private void executeOnTimeout() {
    binaryProcessRequesterProvider.onTimeout();
  }

  private void executeOnDead(int times) {
    for (int i = 0; i < times; i++) {
      executeOnDead();
    }
  }

  private VerificationMode timesBeyondCreation(int times) {
    return times(times + TIMES_OF_INIT_CALLS_ON_CREATION);
  }
}
