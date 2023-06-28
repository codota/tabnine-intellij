package com.tabnineCommon.lifecycle;

import com.intellij.util.messages.Topic;

public interface BinaryCapabilitiesChangeNotifier {
  Topic<BinaryCapabilitiesChangeNotifier> CAPABILITIES_CHANGE_NOTIFIER_TOPIC =
      Topic.create("com.tabnine.capabilities_changed", BinaryCapabilitiesChangeNotifier.class);

  void notifyFetched();
}
