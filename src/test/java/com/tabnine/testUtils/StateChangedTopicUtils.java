package com.tabnine.testUtils;

import com.intellij.openapi.application.ApplicationManager;
import com.tabnine.binary.requests.config.StateResponse;
import com.tabnine.lifecycle.BinaryStateChangeNotifier;

public class StateChangedTopicUtils {

  public static void notifyStateChangedTopic(StateResponse stateResponse) {
    ApplicationManager.getApplication()
        .getMessageBus()
        .syncPublisher(BinaryStateChangeNotifier.STATE_CHANGED_TOPIC)
        .stateChanged(stateResponse);
  }

  public static StateResponse getMockedStateResponse(boolean isConnectionHealthy) {
    return new StateResponse(null, null, null, isConnectionHealthy);
  }
}
