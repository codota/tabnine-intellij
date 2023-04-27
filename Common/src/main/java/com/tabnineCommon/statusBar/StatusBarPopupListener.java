package com.tabnineCommon.statusBar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.statusBar.StatusBarInteractionRequest;
import com.tabnineCommon.general.IBinaryFacadeProvider;
import org.jetbrains.annotations.NotNull;

public class StatusBarPopupListener implements JBPopupListener {
  private final BinaryRequestFacade binaryRequestFacade =
      ApplicationManager.getApplication()
          .getService(IBinaryFacadeProvider.class)
          .getBinaryRequestFacade();

  @Override
  public void beforeShown(@NotNull LightweightWindowEvent event) {
    binaryRequestFacade.executeRequest(new StatusBarInteractionRequest());
  }
}
