package com.tabnine;

import static com.tabnine.general.DependencyContainer.*;

import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.tabnine.capabilities.CapabilitiesService;
import com.tabnine.lifecycle.BinaryNotificationsLifecycle;
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle;
import com.tabnine.lifecycle.TabnineUpdater;
import com.tabnine.logging.LogInitializerKt;
import com.tabnine.state.UserState;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class Initializer extends PreloadingActivity implements StartupActivity {
  private BinaryNotificationsLifecycle binaryNotificationsLifecycle;
  private BinaryPromotionStatusBarLifecycle binaryPromotionStatusBarLifecycle;
  private final AtomicBoolean initialized = new AtomicBoolean(false);

  @Override
  public void preload(@NotNull ProgressIndicator indicator) {
    initialize();
  }

  @Override
  public void runActivity(@NotNull Project project) {
    initialize();
  }

  private void initialize() {
    boolean shouldInitialize =
        !(initialized.getAndSet(true) || ApplicationManager.getApplication().isUnitTestMode());
    if (shouldInitialize) {
      LogInitializerKt.init();
      binaryNotificationsLifecycle = instanceOfBinaryNotifications();
      binaryPromotionStatusBarLifecycle = instanceOfBinaryPromotionStatusBar();
      binaryNotificationsLifecycle.poll();
      binaryPromotionStatusBarLifecycle.poll();
      CapabilitiesService.getInstance().init();
      TabnineUpdater.pollUpdates();
      PluginInstaller.addStateListener(instanceOfUninstallListener());
      UserState.init();
    }
  }
}
