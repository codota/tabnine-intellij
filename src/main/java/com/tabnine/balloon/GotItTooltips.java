package com.tabnine.balloon;

import com.intellij.openapi.editor.Editor;
import com.tabnine.state.UserState;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GotItTooltips {
  public static void showFirstCompletionHint(Editor editor) {
    new GotItTooltip(
            "first-suggestion-hint",
            "Your first completion from Tabnine",
            "Press <b>Tab</b> to accept Tabnine's suggestion",
            () -> UserState.getInstance().getCompletionHintState().setIsCompletionHintShown(true))
        .show(editor);
    Executors.newSingleThreadScheduledExecutor()
        .schedule(
            () -> UserState.getInstance().getCompletionHintState().setIsCompletionHintShown(true),
            30,
            TimeUnit.SECONDS);
  }
}
