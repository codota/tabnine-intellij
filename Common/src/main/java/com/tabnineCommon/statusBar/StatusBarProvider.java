package com.tabnineCommon.statusBar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import com.tabnineCommon.config.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatusBarProvider implements StatusBarWidgetProvider {
  @Nullable
  @Override
  public com.intellij.openapi.wm.StatusBarWidget getWidget(@NotNull Project project) {
    if (Config.IS_SELF_HOSTED) {
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
