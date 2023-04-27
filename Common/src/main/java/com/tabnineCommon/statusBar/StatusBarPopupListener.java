package com.tabnineCommon.statusBar;

import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.statusBar.StatusBarInteractionRequest;
import com.tabnineCommon.general.IProviderOfThings;
import org.jetbrains.annotations.NotNull;

public class StatusBarPopupListener implements JBPopupListener {
  private final BinaryRequestFacade binaryRequestFacade =
      ServiceManager.getService(IProviderOfThings.class).getBinaryRequestFacade();

  @Override
  public void beforeShown(@NotNull LightweightWindowEvent event) {
    binaryRequestFacade.executeRequest(new StatusBarInteractionRequest());
  }
}
