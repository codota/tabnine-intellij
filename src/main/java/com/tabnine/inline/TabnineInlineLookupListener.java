package com.tabnine.inline;

import static com.tabnine.general.DependencyContainer.singletonOfInlineCompletionHandler;

import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public class TabnineInlineLookupListener implements LookupListener {
  private final InlineCompletionHandler handler = singletonOfInlineCompletionHandler();

  @Override
  public void currentItemChanged(@NotNull LookupEvent event) {
    if (!event.getLookup().isFocused() || event.getItem() == null) {
      return;
    }

    Editor editor = event.getLookup().getEditor();
    CompletionPreview.clear(editor);

    String userPrefix = event.getLookup().itemPattern(event.getItem());
    String completionInFocus = event.getItem().getLookupString();
    // a weird case when the user presses ctrl+enter but the popup isn't rendered
    // (DocumentChanged event is triggered in this case)
    if (userPrefix.equals(completionInFocus)) {
      return;
    }

    if (!completionInFocus.startsWith(userPrefix)) {
      return;
    }

    ApplicationManager.getApplication()
        .invokeLater(
            () ->
                handler.retrieveAndShowCompletion(
                    editor,
                    editor.getCaretModel().getOffset(),
                    new LookAheadCompletionAdjustment(userPrefix, completionInFocus)));
  }

  @Override
  public void lookupCanceled(@NotNull LookupEvent event) {
    // Do nothing, but the validator is furious if we don't implement this.
    // Probably because in older versions this was not implemented.
  }

  @Override
  public void itemSelected(@NotNull LookupEvent event) {
    // Do nothing, but the validator is furious if we don't implement this.
    // Probably because in older versions this was not implemented.
  }
}
