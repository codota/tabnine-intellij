package com.tabnine.balloon;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.tabnine.selections.CompletionListener;
import com.tabnine.selections.CompletionObserver;
import com.tabnine.state.UserState;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SuggestionHintTooltip {
  private static final GotItTooltip suggestionHintTooltip =
      new GotItTooltip(
          "first-suggestion-hint",
          "Your first completion from Tabnine",
          "Press <b>Tab</b> to accept Tabnine's suggestion",
          () -> UserState.getInstance().getSuggestionHintState().setHintWasShown());
  private static final int MAX_SECONDS_TO_SHOW_SUGGESTION_HINT = 30;

  public static void handle(Editor editor) {
    try {
      UserState userState = UserState.getInstance();
      if (!userState.getSuggestionHintState().isEligibleForSuggestionHint()) {
        userState.getSuggestionHintState().setHintWasShown();
        return;
      }
      if (suggestionHintTooltip.isVisible()) {
        return;
      }
      suggestionHintTooltip.show(editor);
      CompletionObserver.subscribe(
          new CompletionListener() {
            @Override
            public void onCompletion() {
              UserState.getInstance().getSuggestionHintState().setHintWasShown();
              suggestionHintTooltip.dispose();
              CompletionObserver.unsubscribe(this);
            }
          });
      Executors.newSingleThreadScheduledExecutor()
          .schedule(
              () -> {
                UserState.getInstance().getSuggestionHintState().setHintWasShown();
                suggestionHintTooltip.dispose();
              },
              MAX_SECONDS_TO_SHOW_SUGGESTION_HINT,
              TimeUnit.SECONDS);
    } catch (Exception e) {
      Logger.getInstance(SuggestionHintTooltip.class)
          .warn("Error handling completion hint tooltip", e);
    }
  }
}
