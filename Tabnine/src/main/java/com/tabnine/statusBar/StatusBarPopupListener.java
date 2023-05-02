package com.tabnine.statusBar;

import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.statusBar.StatusBarInteractionRequest;
import com.tabnineCommon.general.DependencyContainer;
import org.jetbrains.annotations.NotNull;

public class StatusBarPopupListener implements JBPopupListener {
  private final BinaryRequestFacade binaryRequestFacade =
      DependencyContainer.instanceOfBinaryRequestFacade();

  @Override
  public void beforeShown(@NotNull LightweightWindowEvent event) {
    binaryRequestFacade.executeRequest(new StatusBarInteractionRequest());
  }
}
