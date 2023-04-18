package com.tabnine.lifecycle;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.messages.MessageBus;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.config.StateRequest;
import com.tabnine.binary.requests.config.StateResponse;
import com.tabnine.general.DependencyContainer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BinaryStateService {
  private static final ScheduledExecutorService scheduler =
      AppExecutorUtil.getAppScheduledExecutorService();
  private final BinaryRequestFacade binaryRequestFacade =
      DependencyContainer.instanceOfBinaryRequestFacade();
  private final MessageBus messageBus;
  private StateResponse lastStateResponse;
  private final AtomicBoolean updateLoopStarted = new AtomicBoolean(false);

  public BinaryStateService() {
    this.messageBus = ApplicationManager.getApplication().getMessageBus();
  }

  public void startUpdateLoop() {
    if (updateLoopStarted.getAndSet(true)) {
      return;
    }
    scheduler.scheduleWithFixedDelay(this::updateState, 0, 2, TimeUnit.SECONDS);
  }

  public StateResponse getLastStateResponse() {
    return this.lastStateResponse;
  }

  private void updateState() {
    final StateResponse stateResponse = this.binaryRequestFacade.executeRequest(new StateRequest());
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
