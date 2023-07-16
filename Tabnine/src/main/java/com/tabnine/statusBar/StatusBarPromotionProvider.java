package com.tabnine.statusBar;

import static com.tabnineCommon.general.DependencyContainer.instanceOfBinaryRequestFacade;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import com.tabnine.lifecycle.BinaryInstantiatedActions;
import com.tabnineCommon.binary.BinaryRequestFacade;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatusBarPromotionProvider implements StatusBarWidgetFactory {
  private final BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();
  private final BinaryInstantiatedActions actionVisitor =
      new BinaryInstantiatedActions(instanceOfBinaryRequestFacade());

  @Override
  public @NotNull String getId() {
    return getClass().getName();
  }

  @Override
  public @Nls @NotNull String getDisplayName() {
    return "Tabnine (promotion)";
  }

  @Override
  public boolean isAvailable(@NotNull Project project) {
    return true;
  }

  @Override
  public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
    Logger.getInstance(getClass()).info("creating (promotion) status bar widget");
    return new StatusBarPromotionWidget(project, binaryRequestFacade, actionVisitor);
  }

  @Override
  public void disposeWidget(@NotNull StatusBarWidget widget) {
    Logger.getInstance(getClass()).info("disposing (promotion) status bar widget");
    Disposer.dispose(widget);
  }

  @Override
  public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
    return true;
  }
}
