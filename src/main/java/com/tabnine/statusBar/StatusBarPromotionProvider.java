package com.tabnine.statusBar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.general.DependencyContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.tabnine.general.DependencyContainer.instanceOfBinaryRequestFacade;

public class StatusBarPromotionProvider implements StatusBarWidgetProvider {
    private BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();

    @Nullable
    @Override
    public com.intellij.openapi.wm.StatusBarWidget getWidget(@NotNull Project project) {
        return new StatusBarPromotionWidget(project, binaryRequestFacade);
    }

    @NotNull
    @Override
    public String getAnchor() {
        return StatusBar.Anchors.after(TabnineStatusBarWidget.class.getName());
    }
}
