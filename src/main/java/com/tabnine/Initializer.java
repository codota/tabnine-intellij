package com.tabnine;

import static com.tabnine.general.DependencyContainer.*;
import static com.tabnine.general.Utils.SetCustomRepository;

import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.tabnine.capabilities.CapabilitiesService;
import com.tabnine.config.Config;
import com.tabnine.dialogs.Dialogs;
import com.tabnine.dialogs.TabnineEnterpriseUrlDialogWrapper;
import com.tabnine.general.StaticConfig;
import com.tabnine.lifecycle.BinaryNotificationsLifecycle;
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle;
import com.tabnine.lifecycle.BinaryStateService;
import com.tabnine.lifecycle.TabnineUpdater;
import com.tabnine.logging.LogInitializerKt;
import com.tabnine.notifications.ConnectionLostNotificationHandler;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tabnine.userSettings.AppSettingsState;
import org.jetbrains.annotations.NotNull;

public class Initializer extends PreloadingActivity implements StartupActivity {
  private BinaryNotificationsLifecycle binaryNotificationsLifecycle;
  private BinaryPromotionStatusBarLifecycle binaryPromotionStatusBarLifecycle;
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private static final ConnectionLostNotificationHandler connectionLostNotificationHandler =
      new ConnectionLostNotificationHandler();

  @Override
  public void preload(@NotNull ProgressIndicator indicator) {
    initialize();
  }

  @Override
  public void runActivity(@NotNull Project project) {
    initialize();
  }

  private static final AtomicBoolean DIALOG_SHOWED = new AtomicBoolean(false);

  private void initialize() {

    boolean shouldInitialize =
        !(initialized.getAndSet(true) || ApplicationManager.getApplication().isUnitTestMode());
    if (shouldInitialize) {
      Logger.getInstance(getClass())
          .info(
              "Initializing for "
                  + Config.CHANNEL
                  + ", plugin id = "
                  + StaticConfig.TABNINE_PLUGIN_ID_RAW);

      connectionLostNotificationHandler.startConnectionLostListener();
      ServiceManager.getService(BinaryStateService.class).startUpdateLoop();

      if (!Config.IS_ON_PREM) {
        LogInitializerKt.init();
        binaryNotificationsLifecycle = instanceOfBinaryNotifications();
        binaryPromotionStatusBarLifecycle = instanceOfBinaryPromotionStatusBar();
        binaryNotificationsLifecycle.poll();
        binaryPromotionStatusBarLifecycle.poll();
        CapabilitiesService.getInstance().init();
        TabnineUpdater.pollUpdates();
        PluginInstaller.addStateListener(instanceOfUninstallListener());
      } else if (Config.IS_ON_PREM && !DIALOG_SHOWED.get()) {
        DIALOG_SHOWED.set(true);
        Optional<String> cloud2Url = StaticConfig.getTabnineEnterpriseHost();
        if (cloud2Url.isPresent()) {
          Logger.getInstance(getClass()).info(String.format("Tabnine Enterprise host is configured: %s", cloud2Url.get()));
          SetCustomRepository(cloud2Url.get());
        } else {
          Logger.getInstance(getClass())
            .warn(
                  "Tabnine Enterprise host is not configured, showing some nice dialog");
          ApplicationManager.getApplication().invokeLater(() -> {
            TabnineEnterpriseUrlDialogWrapper dialog = new TabnineEnterpriseUrlDialogWrapper(null);
            if (dialog.showAndGet()) {
              String url = dialog.getInputData();
              AppSettingsState.getInstance().setCloud2Url(url);
              SetCustomRepository(url);
              Dialogs.showRestartDialog("Self hosted URL configured successfully - Restart your IDE for the change to take effect.");
            }
          });
        }
      }
    }
  }
}
