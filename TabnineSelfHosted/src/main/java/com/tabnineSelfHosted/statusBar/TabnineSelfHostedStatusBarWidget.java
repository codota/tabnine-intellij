package com.tabnineSelfHosted.statusBar;

import static com.tabnineCommon.general.StaticConfig.getTabnineEnterpriseHost;

import com.intellij.ide.DataManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus;
import com.tabnineCommon.general.StaticConfig;
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineSelfHostedStatusBarWidget extends EditorBasedWidget
    implements StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {
  private CloudConnectionHealthStatus cloudConnectionHealthStatus = CloudConnectionHealthStatus.Ok;
  private String username = "";

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
              this.username = stateResponse.getUserName();
              update();
            });
  }

  public Icon getIcon() {
    if (this.cloudConnectionHealthStatus == CloudConnectionHealthStatus.Failed) {
      return StaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE;
    }

    boolean hasCloud2UrlConfigured =
        getTabnineEnterpriseHost().isPresent()
            && !getTabnineEnterpriseHost().get().trim().isEmpty();

    if (hasCloud2UrlConfigured && this.username != null && !this.username.trim().isEmpty()) {
      return StaticConfig.ICON_AND_NAME_ENTERPRISE;
    }
    return StaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE;
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
    if (this.username.trim().isEmpty()) {
      return "Please login for using Tabnine Enterprise";
    }

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

  @Nullable
  public Consumer<MouseEvent> getClickConsumer() {
    return null;
  }

  public @Nullable("null means the widget is unable to show the popup") ListPopup getPopupStep() {
    ListPopup popup =
        JBPopupFactory.getInstance()
            .createActionGroupPopup(
                null,
                SelfHostedStatusBarActions.buildStatusBarActionsGroup(
                    myStatusBar != null ? myStatusBar.getProject() : null,
                    this.username != null && !this.username.trim().isEmpty()),
                DataManager.getInstance()
                    .getDataContext(myStatusBar != null ? myStatusBar.getComponent() : null),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true);
    return popup;
  }

  @Nullable
  @Override
  public String getSelectedValue() {
    return "\u0000";
  }

  private void update() {
    if (myStatusBar == null) {
      Logger.getInstance(getClass()).warn("Failed to update the status bar");
      return;
    }
    myStatusBar.updateWidget(ID());
  }
}
