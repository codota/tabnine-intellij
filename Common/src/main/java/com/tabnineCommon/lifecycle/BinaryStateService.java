package com.tabnineCommon.lifecycle;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.messages.MessageBus;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.config.StateRequest;
import com.tabnineCommon.binary.requests.config.StateResponse;
import com.tabnineCommon.general.IProviderOfThings;
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
    this.messageBus = ServiceManager.getMessageBus();
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
    final BinaryRequestFacade binaryRequestFacade =
        ServiceManager.getService(IProviderOfThings.class).getBinaryRequestFacade();
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
