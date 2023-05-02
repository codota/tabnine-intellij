package com.tabnine.statusBar;

import static com.tabnineCommon.general.DependencyContainer.instanceOfBinaryRequestFacade;
import static com.tabnineCommon.general.DependencyContainer.instanceOfGlobalActionVisitor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.config.Config;
import com.tabnineCommon.lifecycle.BinaryInstantiatedActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatusBarPromotionProvider implements StatusBarWidgetProvider {
  private final BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();
  private final BinaryInstantiatedActions actionVisitor = instanceOfGlobalActionVisitor();

  @Nullable
  @Override
  public com.intellij.openapi.wm.StatusBarWidget getWidget(@NotNull Project project) {
    if (Config.IS_SELF_HOSTED) {
      return null;
    }
    return new StatusBarPromotionWidget(project, binaryRequestFacade, actionVisitor);
  }

  @NotNull
  @Override
  public String getAnchor() {
    return StatusBar.Anchors.after(TabnineStatusBarWidget.class.getName());
  }
}
