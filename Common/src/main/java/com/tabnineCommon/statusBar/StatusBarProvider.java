package com.tabnineCommon.statusBar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import com.tabnineCommon.config.Config;
import com.tabnineCommon.general.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatusBarProvider implements StatusBarWidgetProvider {
  @Nullable
  @Override
  public com.intellij.openapi.wm.StatusBarWidget getWidget(@NotNull Project project) {
    if (Utils.isSelfHostedPlugin()) {
      return new TabnineEnterpriseStatusBarWidget(project);
    }
    return new TabnineStatusBarWidget(project);
  }

  @NotNull
  @Override
  public String getAnchor() {
    return StatusBar.Anchors.before(StatusBar.StandardWidgets.POSITION_PANEL);
  }
}
