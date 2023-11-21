package com.tabnineCommon.lifecycle;

import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.config.StateRequest;
import com.tabnineCommon.binary.requests.config.StateResponse;
import com.tabnineCommon.general.DependencyContainer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BinaryStateService {
  private static final ScheduledExecutorService scheduler =
      AppExecutorUtil.getAppScheduledExecutorService();
  private final BinaryRequestFacade binaryRequestFacade =
      DependencyContainer.instanceOfBinaryRequestFacade();
  private final AtomicBoolean updateLoopStarted = new AtomicBoolean(false);

  public void startUpdateLoop() {
    if (updateLoopStarted.getAndSet(true)) {
      return;
    }
    scheduler.scheduleWithFixedDelay(this::updateState, 0, 2, TimeUnit.SECONDS);
  }

  private void updateState() {
    final StateResponse stateResponse = this.binaryRequestFacade.executeRequest(new StateRequest());

    if (stateResponse != null) {
      BinaryStateSingleton.getInstance().set(stateResponse);
    }
  }
}
