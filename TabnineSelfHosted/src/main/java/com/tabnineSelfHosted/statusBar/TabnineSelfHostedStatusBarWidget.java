package com.tabnineSelfHosted.statusBar;

import static com.tabnine.general.StaticConfig.getTabnineEnterpriseHost;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.tabnine.binary.requests.config.CloudConnectionHealthStatus;
import com.tabnine.general.SubscriptionType;
import com.tabnine.lifecycle.BinaryStateChangeNotifier;
import com.tabnine.userSettings.AppSettingsConfigurable;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineSelfHostedStatusBarWidget extends EditorBasedWidget
    implements StatusBarWidget, StatusBarWidget.IconPresentation {
  private CloudConnectionHealthStatus cloudConnectionHealthStatus = CloudConnectionHealthStatus.Ok;

  public TabnineSelfHostedStatusBarWidget(@NotNull Project project) {
    super(project);
    // register for state changes (we will get notified whenever the state changes)
    ApplicationManager.getApplication()
        .getMessageBus()
        .connect(this)
        .subscribe(
            BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
            stateResponse -> {
              this.cloudConnectionHealthStatus = stateResponse.getCloudConnectionHealthStatus();
              update();
            });
  }

  public Icon getIcon() {
    return SubscriptionType.Enterprise.getTabnineLogo(this.cloudConnectionHealthStatus);
  }

  // Compatability implementation. DO NOT ADD @Override.
  @Nullable
  public WidgetPresentation getPresentation() {
    return this;
  }

  // Compatability implementation. DO NOT ADD @Override.
  @Nullable
  public WidgetPresentation getPresentation(@NotNull PlatformType type) {
    return this;
  }

  @NotNull
  @Override
  public String ID() {
    return getClass().getName();
  }

  // Compatability implementation. DO NOT ADD @Override.
  @Nullable
  public String getTooltipText() {
    String enterpriseHostDisplayString =
        getTabnineEnterpriseHost()
            .map(
                host -> {
                  String prefix =
                      this.cloudConnectionHealthStatus == CloudConnectionHealthStatus.Failed
                          ? "(connection failed to host '"
                          : "(host='";
                  return prefix + host + "')";
                })
            .orElse("(host is not set)");
    return "Open Tabnine Settings " + enterpriseHostDisplayString;
  }

  // Compatability implementation. DO NOT ADD @Override.
  @Nullable
  public Consumer<MouseEvent> getClickConsumer() {
    return mouseEvent -> {
      if (mouseEvent.isPopupTrigger() || MouseEvent.BUTTON1 != mouseEvent.getButton()) {
        return;
      }
      Logger.getInstance(getClass()).info("Opening Tabnine settings");
      ShowSettingsUtil.getInstance()
          .editConfigurable(this.myProject, new AppSettingsConfigurable());
    };
  }

  private void update() {
    if (myStatusBar == null) {
      Logger.getInstance(getClass()).warn("Failed to update the status bar");
      return;
    }
    myStatusBar.updateWidget(ID());
  }
}
