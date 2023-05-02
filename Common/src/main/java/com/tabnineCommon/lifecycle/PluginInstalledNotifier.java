package com.tabnineCommon.lifecycle;

import com.intellij.util.messages.Topic;

public interface PluginInstalledNotifier {
  Topic<PluginInstalledNotifier> PLUGIN_INSTALLED_TOPIC =
      Topic.create("Plugin Installed Notifier", PluginInstalledNotifier.class);

  void onPluginInstalled();
}
