package com.tabnine.statusBar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.util.Consumer;
import com.tabnine.LimitedSecletionsChangedNotifier;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.config.ConfigRequest;
import com.tabnine.binary.requests.config.StateResponse;
import com.tabnine.binary.requests.statusBar.ConfigOpenedFromStatusBarRequest;
import com.tabnine.general.ServiceLevel;
import com.tabnine.general.StaticConfig;
import com.tabnine.lifecycle.BinaryStateChangeNotifier;
import com.tabnine.lifecycle.BinaryStateService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import static com.tabnine.general.StaticConfig.*;

public class TabnineStatusBarWidget extends EditorBasedWidget implements CustomStatusBarWidget,
        com.intellij.openapi.wm.StatusBarWidget.WidgetPresentation {
    private final BinaryRequestFacade binaryRequestFacade;
    private TextPanel.WithIconAndArrows component;

    public TabnineStatusBarWidget(@NotNull Project project, BinaryRequestFacade binaryRequestFacade) {
        super(project);
        this.binaryRequestFacade = binaryRequestFacade;
        //register for state changes (we will get notified whenever the state changes)
        ApplicationManager.getApplication().getMessageBus().connect(this)
                .subscribe(BinaryStateChangeNotifier.STATE_CHANGED_TOPIC, stateResponse -> update());
        ApplicationManager.getApplication().getMessageBus().connect(this)
                .subscribe(LimitedSecletionsChangedNotifier.LIMITED_SELECTIONS_CHANGED_TOPIC, this::update);
    }

    @NotNull
    @Override
    public String ID() {
        return getClass().getName();
    }

    // Compatability implementation. DO NOT ADD @Override.
    public JComponent getComponent() {
        final TextPanel.WithIconAndArrows component = new TextPanel.WithIconAndArrows();
        final Icon icon = getIcon(getServiceLevel());
        component.setIcon(icon);
        component.setToolTipText(getTooltipText());
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                Objects.requireNonNull(getClickConsumer()).consume(e);
            }
        });
        this.component = component;
        return component;
    }

    private Icon getIcon(ServiceLevel serviceLevel) {
        if (serviceLevel == ServiceLevel.PRO) {
            return ICON_AND_NAME_PRO;
        } else {
            return ICON_AND_NAME;
        }
    }

    private ServiceLevel getServiceLevel() {
        final StateResponse stateResponse = ServiceManager.getService(BinaryStateService.class).getLastStateResponse();
        return stateResponse != null ? stateResponse.getServiceLevel() : null;
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

    // Compatability implementation. DO NOT ADD @Override.
    @Nullable
    public String getTooltipText() {
        return "Tabnine (Click to open settings)";
    }

    // Compatability implementation. DO NOT ADD @Override.
    @Nullable
    public Consumer<MouseEvent> getClickConsumer() {
        return (e) -> {
            if (!e.isPopupTrigger() && MouseEvent.BUTTON1 == e.getButton()) {
                binaryRequestFacade.executeRequest(new ConfigRequest());
                binaryRequestFacade.executeRequest(new ConfigOpenedFromStatusBarRequest());
            }
        };
    }

    private void update(boolean limited) {
        if (limited) {
            this.component.setText(LIMITATION_SYMBOL);
        } else {
            this.component.setText(null);
        }
        update();
    }

    private void update() {
        ApplicationManager.getApplication().invokeLater(() -> {
            //noinspection ConstantConditions
            if ((myProject == null) || myProject.isDisposed() || (myStatusBar == null)) {
                return;
            }
            final ServiceLevel serviceLevel = getServiceLevel();
            final Icon icon = getIcon(serviceLevel);
            this.component.setIcon(icon);
            if (serviceLevel == ServiceLevel.PRO) {
                //remove the locked icon. We do this here to handle the case where service
                //level changed but limited wasn't updated yet (i.e. user didn't perform a
                //completion yet).
                component.setText(null);
            }
            this.component.setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            myStatusBar.updateWidget(ID());
            //Since the widget size changes, we need to repaint the whole status bar so it will
            //be positioned correctly
            final StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
            if (statusBar != null) {
                statusBar.getComponent().updateUI();
            }
        }, ModalityState.any());
    }


}
