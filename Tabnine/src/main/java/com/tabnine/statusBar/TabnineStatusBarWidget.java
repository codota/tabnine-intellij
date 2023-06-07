package com.tabnine.statusBar;

import static com.tabnineCommon.general.StaticConfig.*;
import static com.tabnineCommon.general.SubscriptionTypeKt.getSubscriptionType;

import com.intellij.ide.DataManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.tabnine.intellij.completions.LimitedSecletionsChangedNotifier;
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus;
import com.tabnineCommon.binary.requests.config.StateResponse;
import com.tabnineCommon.capabilities.CapabilitiesService;
import com.tabnineCommon.capabilities.Capability;
import com.tabnineCommon.general.ServiceLevel;
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier;
import com.tabnineCommon.lifecycle.BinaryStateService;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineStatusBarWidget extends EditorBasedWidget
    implements StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {
  private static final String EMPTY_SYMBOL = "\u0000";
  private boolean isLimited = false;

  private Boolean isLoggedIn = null;
  private CloudConnectionHealthStatus cloudConnectionHealthStatus = CloudConnectionHealthStatus.Ok;

  public TabnineStatusBarWidget(@NotNull Project project) {
    super(project);
    // register for state changes (we will get notified whenever the state changes)
    ApplicationManager.getApplication()
        .getMessageBus()
        .connect(this)
        .subscribe(
            BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
            stateResponse -> {
              this.isLoggedIn = stateResponse.isLoggedIn();
              this.cloudConnectionHealthStatus = stateResponse.getCloudConnectionHealthStatus();
              update();
            });
    ApplicationManager.getApplication()
        .getMessageBus()
        .connect(this)
        .subscribe(
            LimitedSecletionsChangedNotifier.LIMITED_SELECTIONS_CHANGED_TOPIC,
            limited -> {
              this.isLimited = limited;
              update();
            });
  }

  public Icon getIcon() {
    return getSubscriptionType(getServiceLevel()).getTabnineLogo(this.cloudConnectionHealthStatus);
  }

  public @Nullable("null means the widget is unable to show the popup") ListPopup getPopupStep() {
    return createPopup();
  }

  public String getSelectedValue() {
    if (this.cloudConnectionHealthStatus != CloudConnectionHealthStatus.Ok) {
      return "Server connectivity issue";
    }
    return this.isLimited ? LIMITATION_SYMBOL : EMPTY_SYMBOL;
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

  private ListPopup createPopup() {
    ListPopup popup =
        JBPopupFactory.getInstance()
            .createActionGroupPopup(
                null,
                StatusBarActions.buildStatusBarActionsGroup(
                    myStatusBar != null ? myStatusBar.getProject() : null),
                DataManager.getInstance()
                    .getDataContext(myStatusBar != null ? myStatusBar.getComponent() : null),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true);
    popup.addListener(new StatusBarPopupListener());
    return popup;
  }

  private ServiceLevel getServiceLevel() {
    StateResponse stateResponse =
        ServiceManager.getService(BinaryStateService.class).getLastStateResponse();
    return stateResponse != null ? stateResponse.getServiceLevel() : null;
  }

  // Compatability implementation. DO NOT ADD @Override.
  @Nullable
  public String getTooltipText() {
    if (this.isLoggedIn != null
        && !this.isLoggedIn
        && CapabilitiesService.getInstance().isCapabilityEnabled(Capability.FORCE_REGISTRATION)) {
      return "Sign in using your Tabnine account";
    }
    return "Tabnine (Click to open settings)";
  }

  // Compatability implementation. DO NOT ADD @Override.
  @Nullable
  public Consumer<MouseEvent> getClickConsumer() {
    return null;
  }

  private void update() {
    if (myStatusBar == null) {
      Logger.getInstance(getClass()).warn("Failed to update the status bar");
      return;
    }
    myStatusBar.updateWidget(ID());
  }
}
