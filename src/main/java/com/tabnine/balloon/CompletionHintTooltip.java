package com.tabnine.balloon;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.tabnine.selections.CompletionListener;
import com.tabnine.selections.CompletionObserver;
import com.tabnine.state.UserState;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CompletionHintTooltip {
  private static final GotItTooltip completionHintTooltip =
      new GotItTooltip(
          "first-completion-hint",
          "Your first completion from Tabnine",
          "Press <b>Tab</b> to accept Tabnine's suggestion",
          () -> UserState.getInstance().getCompletionHintState().setIsShown(true));

  public static void handle(Editor editor) {
    try {
      UserState userState = UserState.getInstance();
      if (userState.getCompletionHintState().isEligibleForCompletionHint()) {
        if (!completionHintTooltip.isVisible()) {
          completionHintTooltip.show(editor);
          CompletionObserver.subscribe(
              new CompletionListener() {
                @Override
                public void onCompletion() {
                  UserState.getInstance().getCompletionHintState().setIsShown(true);
                  completionHintTooltip.dispose();
                  CompletionObserver.unsubscribe(this);
                }
              });
          Executors.newSingleThreadScheduledExecutor()
              .schedule(
                  () -> UserState.getInstance().getCompletionHintState().setIsShown(true),
                  30,
                  TimeUnit.SECONDS);
        }
      } else {
        userState.getCompletionHintState().setIsShown(true);
      }
    } catch (Exception e) {
      Logger.getInstance(CompletionHintTooltip.class)
          .warn("Error handling completion hint tooltip", e);
    }
  }
}
