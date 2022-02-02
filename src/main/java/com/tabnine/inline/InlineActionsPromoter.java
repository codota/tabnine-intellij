package com.tabnine.inline;

import com.intellij.openapi.actionSystem.ActionPromoter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InlineActionsPromoter implements ActionPromoter {

  @Override
  public List<AnAction> promote(List<AnAction> actions, DataContext context) {
    Editor editor = CommonDataKeys.EDITOR.getData(context);
    if (editor != null) {
      CompletionPreview preview = CompletionPreview.findCompletionPreview(editor);
      if (preview != null) {
        return actions.stream()
            .filter(action -> action instanceof InlineCompletionAction)
            .collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }
}
