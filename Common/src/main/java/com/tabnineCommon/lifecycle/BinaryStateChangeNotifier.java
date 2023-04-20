package com.tabnineCommon.lifecycle;

import com.intellij.util.messages.Topic;
import com.tabnineCommon.binary.requests.config.StateResponse;

public interface BinaryStateChangeNotifier {
  Topic<BinaryStateChangeNotifier> STATE_CHANGED_TOPIC =
      Topic.create("Binary State Changed Notifier", BinaryStateChangeNotifier.class);

  void stateChanged(StateResponse state);
}
