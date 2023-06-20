package com.tabnineCommon.lifecycle;

import com.intellij.util.messages.Topic;
@Deprecated
public interface PluginInstalledNotifier {
  @Deprecated
  Topic<PluginInstalledNotifier> PLUGIN_INSTALLED_TOPIC =
      Topic.create("Plugin Installed Notifier", PluginInstalledNotifier.class);

  void onPluginInstalled();
}
