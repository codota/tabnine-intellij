package com.tabnine.testUtils;

import com.intellij.openapi.application.ApplicationManager;
import com.tabnine.binary.requests.config.CloudConnectionHealthStatus;
import com.tabnine.binary.requests.config.StateResponse;
import com.tabnine.lifecycle.BinaryStateChangeNotifier;

public class HealthCheckTestUtils {

  public static void notifyHealthStatusLoop(
      CloudConnectionHealthStatus cloudConnectionHealthStatus, int times) {
    for (int i = 0; i < times; i++) {
      notifyHealthStatus(cloudConnectionHealthStatus);
    }
  }

  public static void notifyHealthStatus(CloudConnectionHealthStatus cloudConnectionHealthStatus) {
    ApplicationManager.getApplication()
        .getMessageBus()
        .syncPublisher(BinaryStateChangeNotifier.STATE_CHANGED_TOPIC)
        .stateChanged(new StateResponse(null, null, null, cloudConnectionHealthStatus));
  }
}
