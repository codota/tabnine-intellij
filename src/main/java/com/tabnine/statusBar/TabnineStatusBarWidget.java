package com.tabnine.statusBar;

import static com.tabnine.general.StaticConfig.*;

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
import com.tabnine.binary.requests.config.StateResponse;
import com.tabnine.general.ServiceLevel;
import com.tabnine.intellij.completions.LimitedSecletionsChangedNotifier;
import com.tabnine.lifecycle.BinaryStateChangeNotifier;
import com.tabnine.lifecycle.BinaryStateService;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineStatusBarWidget extends EditorBasedWidget
    implements StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {
  private final StatusBarEmptySymbolGenerator emptySymbolGenerator =
      new StatusBarEmptySymbolGenerator();
  private boolean isLimited = false;
  private boolean isConnectionHealthy = true;

  public TabnineStatusBarWidget(@NotNull Project project) {
    super(project);
    // register for state changes (we will get notified whenever the state changes)
    ApplicationManager.getApplication()
        .getMessageBus()
        .connect(this)
        .subscribe(
            BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
            stateResponse -> {
              Boolean connectionHealthy = stateResponse.isConnectionHealthy();
              this.isConnectionHealthy = connectionHealthy == null || connectionHealthy;
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
    return getTabnineLogo(getServiceLevel(getStateResponse()), this.isConnectionHealthy);
  }

  public @Nullable("null means the widget is unable to show the popup") ListPopup getPopupStep() {
    return createPopup();
  }

  public String getSelectedValue() {
    return this.isLimited ? LIMITATION_SYMBOL : emptySymbolGenerator.getEmptySymbol();
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

  private StateResponse getStateResponse() {
    return ServiceManager.getService(BinaryStateService.class).getLastStateResponse();
  }

  private ServiceLevel getServiceLevel(StateResponse state) {
    return state != null ? state.getServiceLevel() : null;
  }

  // Compatability implementation. DO NOT ADD @Override.
  @Nullable
  public String getTooltipText() {
    return "Tabnine (Click to open settings)";
  }

  @Override
  public @Nullable Consumer<MouseEvent> getClickConsumer() {
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
