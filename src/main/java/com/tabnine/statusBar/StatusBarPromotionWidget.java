package com.tabnine.statusBar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.util.Consumer;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.statusBar.StatusBarPromotionActionRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import static com.tabnine.general.StaticConfig.PROMOTION_TEXT_COLOR;

public class StatusBarPromotionWidget extends EditorBasedWidget implements CustomStatusBarWidget, StatusBarWidget.WidgetPresentation {
    private StatusBarPromotionComponent component = null;
    private final BinaryRequestFacade binaryRequestFacade;

    public StatusBarPromotionWidget(@NotNull Project project, BinaryRequestFacade binaryRequestFacade) {
        super(project);
        this.binaryRequestFacade = binaryRequestFacade;
    }

    @NotNull
    @Override
    public String ID() {
        return getClass().getName();
    }

    // Compatability implementation. DO NOT ADD @Override.
    public JComponent getComponent() {
        if(component != null) {
            return component;
        }

        component = new StatusBarPromotionComponent();

        component.setForeground(PROMOTION_TEXT_COLOR);
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
                binaryRequestFacade.executeRequest(new StatusBarPromotionActionRequest(component.getId(), component.getText()));
            }
        };
    }

    public static class StatusBarPromotionComponent extends TextPanel.WithIconAndArrows {
        @Nullable
        private String id;

        public @Nullable String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }
    }
}
