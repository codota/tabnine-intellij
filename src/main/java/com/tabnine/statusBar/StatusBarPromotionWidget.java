package com.tabnine.statusBar;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.util.Consumer;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.statusBar.StatusBarPromotionActionRequest;
import com.tabnine.general.StaticConfig;
import com.tabnine.lifecycle.BinaryInstantiatedActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.List;

import static com.tabnine.general.StaticConfig.PROMOTION_LIGHT_TEXT_COLOR;
import static com.tabnine.general.StaticConfig.PROMOTION_TEXT_COLOR;

public class StatusBarPromotionWidget extends EditorBasedWidget implements CustomStatusBarWidget, StatusBarWidget.WidgetPresentation {
    private final BinaryRequestFacade binaryRequestFacade;
    private final BinaryInstantiatedActions actionVisitor;
    private StatusBarPromotionComponent component = null;

    public StatusBarPromotionWidget(@NotNull Project project, BinaryRequestFacade binaryRequestFacade, BinaryInstantiatedActions actionVisitor) {
        super(project);
        this.binaryRequestFacade = binaryRequestFacade;
        this.actionVisitor = actionVisitor;
    }

    @NotNull
    @Override
    public String ID() {
        return getClass().getName();
    }

    // Compatability implementation. DO NOT ADD @Override.
    @NotNull
    public JComponent getComponent() {
        if (component != null) {
            return component;
        }

        component = new StatusBarPromotionComponent();


        component.setForeground(EditorColorsManager.getInstance().isDarkEditor() ? PROMOTION_LIGHT_TEXT_COLOR : PROMOTION_TEXT_COLOR);
        component.setToolTipText(getTooltipText());
        component.setText(null);
        component.setVisible(false);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                Objects.requireNonNull(getClickConsumer()).consume(e);
            }
        });

        return component;
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
        return "Tabnine (Click to open)";
    }

    // Compatability implementation. DO NOT ADD @Override.
    @Nullable
    public Consumer<MouseEvent> getClickConsumer() {
        return e -> {
            if (!e.isPopupTrigger() && MouseEvent.BUTTON1 == e.getButton()) {
                binaryRequestFacade.executeRequest(new StatusBarPromotionActionRequest(component.getId(), component.getText(), component.getActions()));
                if (component.getActions().contains(StaticConfig.OPEN_HUB_ACTION)) {
                    actionVisitor.openHub();
                }
            }
        };
    }

    public static class StatusBarPromotionComponent extends TextPanel.WithIconAndArrows {
        @Nullable
        private String id;
        @Nullable
        private List<String> actions;
        @Nullable
        private String notificationType;

        public @Nullable String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        public @Nullable List<String> getActions() {
            return actions;
        }

        public void setActions(@Nullable List<String> actions) {
            this.actions = actions;
        }

        public @Nullable String getNotificationType() {
            return notificationType;
        }

        public void setNotificationType(@Nullable String notificationType) {
            this.notificationType = notificationType;
        }
    }
}
