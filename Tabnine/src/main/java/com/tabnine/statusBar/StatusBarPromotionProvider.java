package com.tabnine.statusBar;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.general.IProviderOfThings;
import com.tabnineCommon.lifecycle.IBinaryInstantiatedActions;
import com.tabnineCommon.statusBar.StatusBarPromotionWidget;
import com.tabnineCommon.statusBar.TabnineStatusBarWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatusBarPromotionProvider implements StatusBarWidgetProvider {
  private final BinaryRequestFacade binaryRequestFacade = ServiceManager.getService(IProviderOfThings.class).getBinaryRequestFacade();
  private final IBinaryInstantiatedActions actionVisitor = ServiceManager.getService(IProviderOfThings.class).getActionVisitor();

  @Nullable
  @Override
  public com.intellij.openapi.wm.StatusBarWidget getWidget(@NotNull Project project) {
    return new StatusBarPromotionWidget(project, binaryRequestFacade, actionVisitor);
  }

  @NotNull
  @Override
  public String getAnchor() {
    return StatusBar.Anchors.after(TabnineStatusBarWidget.class.getName());
  }
}
