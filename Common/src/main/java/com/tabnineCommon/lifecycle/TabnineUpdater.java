package com.tabnineCommon.lifecycle;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import com.intellij.openapi.updateSettings.impl.UpdateChecker;
import com.intellij.openapi.updateSettings.impl.UpdateInstaller;
import com.intellij.util.Alarm;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TabnineUpdater {
  private Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
  private static TabnineUpdater instance = new TabnineUpdater();
  private static AtomicBoolean initialized = new AtomicBoolean(false);

  public static void pollUpdates() {
    if (initialized.compareAndSet(false, true)) {
      instance.enqueueUpdate(60 * 3 * 1000, 3);
    }
  }

  private void enqueueUpdate(long delay, long retries) {
    alarm.addRequest(
        () -> {
          try {
            tryUpdate();
          } catch (Exception e) {
            Logger.getInstance(getClass()).warn("Error fetching updates.", e);
            if (retries > 0) {
              Logger.getInstance(getClass()).warn("Will try again.");
              enqueueUpdate(60 * 1000, retries - 1);
              return;
            }
          }

          enqueueUpdate(24 * 60 * 60 * 1000, 3);
        },
        delay);
  }

  private void tryUpdate() {
    PluginId pluginId = PluginManager.getPluginByClassName(TabnineUpdater.class.getName());
    String pluginIdString = (pluginId != null) ? pluginId.getIdString() : "com.tabnine.TabNine";
    List<String> plugins = Collections.singletonList(pluginIdString);
    ApplicationManager.getApplication()
        .executeOnPooledThread(
            () -> {
              Application application = ApplicationManager.getApplication();
              // in the future we can add this: || !UpdateSettings.getInstance().isCheckNeeded()
              if (application == null
                  || application.isDisposed()
                  || application.isDisposeInProgress()) {
                // we won't perform the update now
                return;
              }

              Collection<PluginDownloader> availableUpdates = UpdateChecker.getPluginUpdates();

              if (availableUpdates == null) {
                return;
              }

              List<PluginDownloader> pluginsToUpdate =
                  availableUpdates.stream()
                      .filter(downloader -> plugins.contains(downloader.getPluginId()))
                      .collect(Collectors.toList());

              if (pluginsToUpdate.isEmpty()) {

                return;
              }
              UpdateInstaller.installPluginUpdates(pluginsToUpdate, new EmptyProgressIndicator());
            });
  }
}
