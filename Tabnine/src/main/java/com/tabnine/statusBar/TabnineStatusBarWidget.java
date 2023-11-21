package com.tabnine.statusBar;

import static com.tabnineCommon.general.StaticConfig.LIMITATION_SYMBOL;

import com.intellij.ide.DataManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.tabnine.intellij.completions.LimitedSecletionsChangedNotifier;
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus;
import com.tabnineCommon.binary.requests.config.StateResponse;
import com.tabnineCommon.capabilities.CapabilitiesService;
import com.tabnineCommon.capabilities.Capability;
import com.tabnineCommon.capabilities.CapabilityNotifier;
import com.tabnineCommon.general.ServiceLevel;
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier;
import com.tabnineCommon.lifecycle.BinaryStateService;
import com.tabnineCommon.state.CompletionsState;
import com.tabnineCommon.state.CompletionsStateNotifier;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineStatusBarWidget extends EditorBasedWidget
    implements StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {
  private static final String EMPTY_SYMBOL = "\u0000";
  private boolean isLimited = false;

  private Boolean isLoggedIn = getLastBinaryState().map(StateResponse::isLoggedIn).orElse(null);

  private ServiceLevel serviceLevel =
      getLastBinaryState().map(StateResponse::getServiceLevel).orElse(null);
  private CloudConnectionHealthStatus cloudConnectionHealthStatus =
      getLastBinaryState()
          .map(StateResponse::getCloudConnectionHealthStatus)
          .orElse(CloudConnectionHealthStatus.Ok);

  @Nullable private Boolean isForcedRegistration = null;

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
              this.serviceLevel = stateResponse.getServiceLevel();
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

    CapabilityNotifier.Companion.subscribe(
        capabilities -> {
          if (capabilities.isReady()) {
            boolean newForceRegistration = capabilities.isEnabled(Capability.FORCE_REGISTRATION);

            if (isForcedRegistration == null || isForcedRegistration != newForceRegistration) {
              isForcedRegistration = newForceRegistration;
              update();
            }
          }
        });

    CompletionsStateNotifier.Companion.subscribe(isEnabled -> update());
  }

  public Icon getIcon() {
    Icon icon =
        TabnineIconProvider.getIcon(
            this.serviceLevel,
            this.isLoggedIn,
            this.cloudConnectionHealthStatus,
            this.isForcedRegistration);
    if (!CompletionsState.INSTANCE.isCompletionsEnabled()) {
      return IconLoader.getTransparentIcon(icon, 0.3f);
    }
    return icon;
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

  private static Optional<StateResponse> getLastBinaryState() {
    return Optional.ofNullable(
        ServiceManager.getService(BinaryStateService.class).getLastStateResponse());
  }
}
