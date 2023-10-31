package com.tabnineCommon.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages.InputDialog;
import com.tabnineCommon.binary.requests.login.LoginWithCustomTokenRequest;
import com.tabnineCommon.general.DependencyContainer;
import org.jetbrains.annotations.NotNull;

public class ManualLoginAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    InputDialog dialog =
        new InputDialog(
            "Enter your auth token", "Tabnine: Login with auth token", null, null, null);
    if (dialog.showAndGet()) {
      LoginWithCustomTokenRequest request = new LoginWithCustomTokenRequest();
      request.setCustomToken(dialog.getInputString());
      DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(request);
    }
  }
}
