package com.tabnineCommon.actions;

import com.google.common.base.Strings;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.tabnineCommon.binary.requests.login.LoginWithCustomTokenRequest;
import com.tabnineCommon.binary.requests.login.LoginWithCustomTokenUrlRequest;
import com.tabnineCommon.general.DependencyContainer;
import org.jetbrains.annotations.NotNull;

public class ManualLoginAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    String url =
        DependencyContainer.instanceOfBinaryRequestFacade()
            .executeRequest(new LoginWithCustomTokenUrlRequest());

    int dialogResult =
        Messages.showDialog(
            "If already have an auth token, click \"Sign in\" to apply it. Otherwise, click on \"Get auth token\" to get one",
            "Tabnine: Sign in using auth token",
            new String[] {"Sign in", "Get auth token"},
            0,
            null);

    if (dialogResult == 1) {
      BrowserUtil.browse(url);
    }

    String customToken =
        Messages.showInputDialog(
            "Enter your auth token", "Tabnine: Sign in using auth token", null, null, null);

    if (!Strings.isNullOrEmpty(customToken)) {
      LoginWithCustomTokenRequest request = new LoginWithCustomTokenRequest();
      request.setCustomToken(customToken);
      DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(request);
    }
  }
}
