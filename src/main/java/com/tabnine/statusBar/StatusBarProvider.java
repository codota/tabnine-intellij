package com.tabnine.statusBar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.general.DependencyContainer;
import com.tabnine.statusBar.TabnineStatusBarWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatusBarProvider implements StatusBarWidgetProvider {
    private BinaryRequestFacade binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade();

    @Nullable
    @Override
    public com.intellij.openapi.wm.StatusBarWidget getWidget(@NotNull Project project) {
        return new TabnineStatusBarWidget(project, binaryRequestFacade);
    }

    @NotNull
    @Override
    public String getAnchor() {
        return StatusBar.Anchors.before(StatusBar.StandardWidgets.POSITION_PANEL);
    }
}
