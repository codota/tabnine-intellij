package com.tabnine.inline;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.tabnine.general.CompletionsEventSender;
import com.tabnine.general.DependencyContainer;
import org.jetbrains.annotations.NotNull;

public class EscapeHandler extends EditorActionHandler {
  public static final String ACTION_ID = "EditorEscape";
  private final EditorActionHandler myOriginalHandler;
  private final CompletionsEventSender completionsEventSender =
      DependencyContainer.instanceOfCompletionsEventSender();

  public EscapeHandler(EditorActionHandler originalHandler) {
    myOriginalHandler = originalHandler;
  }

  @Override
  public void doExecute(@NotNull Editor editor, Caret caret, DataContext dataContext) {
    CompletionPreview.clear(editor);
    completionsEventSender.sendCancelSuggestionTrigger();
    if (myOriginalHandler.isEnabled(editor, caret, dataContext)) {
      myOriginalHandler.execute(editor, caret, dataContext);
    }
  }

  @Override
  public boolean isEnabledForCaret(
      @NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
    CompletionPreview preview = CompletionPreview.getInstance(editor);
    if (preview != null) {
      return true;
    }
    return myOriginalHandler.isEnabled(editor, caret, dataContext);
  }
}
