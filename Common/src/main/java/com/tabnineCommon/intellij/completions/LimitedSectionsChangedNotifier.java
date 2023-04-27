package com.tabnineCommon.intellij.completions;

import com.intellij.util.messages.Topic;

public interface LimitedSectionsChangedNotifier {
  Topic<LimitedSectionsChangedNotifier> LIMITED_SELECTIONS_CHANGED_TOPIC =
      Topic.create("Limited Selections Changed Notifier", LimitedSectionsChangedNotifier.class);

  void limitedChanged(boolean limited);
}
