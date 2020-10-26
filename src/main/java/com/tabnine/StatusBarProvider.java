package com.tabnine;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.general.DependencyContainer;
import com.tabnine.lifecycle.TabNineStatusBarWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatusBarProvider implements StatusBarWidgetProvider {
    private BinaryRequestFacade binaryRequestFacade = DependencyContainer.singletonOfBinaryRequestFacade();

    @Nullable
    @Override
    public StatusBarWidget getWidget(@NotNull Project project) {
        return new TabNineStatusBarWidget(project, binaryRequestFacade);
    }

    @NotNull
    @Override
    public String getAnchor() {
        return StatusBar.Anchors.before(StatusBar.StandardWidgets.POSITION_PANEL);
    }
}
