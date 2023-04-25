package com.tabnineCommon.lifecycle;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.messages.MessageBus;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.config.StateRequest;
import com.tabnineCommon.binary.requests.config.StateResponse;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BinaryStateService {
  private static final ScheduledExecutorService scheduler =
      AppExecutorUtil.getAppScheduledExecutorService();
  private final MessageBus messageBus;
  private StateResponse lastStateResponse;
  private final AtomicBoolean updateLoopStarted = new AtomicBoolean(false);

  public BinaryStateService() {
    this.messageBus = ApplicationManager.getApplication().getMessageBus();
  }

  public void startUpdateLoop(BinaryRequestFacade binaryRequestFacade) {
    if (updateLoopStarted.getAndSet(true)) {
      return;
    }
    scheduler.scheduleWithFixedDelay(
        () -> updateState(binaryRequestFacade), 0, 2, TimeUnit.SECONDS);
  }

  public StateResponse getLastStateResponse() {
    return this.lastStateResponse;
  }

  private void updateState(BinaryRequestFacade binaryRequestFacade) {
    final StateResponse stateResponse = binaryRequestFacade.executeRequest(new StateRequest());
    if (stateResponse != null) {
      if (!stateResponse.equals(this.lastStateResponse)) {
        this.messageBus
            .syncPublisher(BinaryStateChangeNotifier.STATE_CHANGED_TOPIC)
            .stateChanged(stateResponse);
      }
      this.lastStateResponse = stateResponse;
    }
  }
}
