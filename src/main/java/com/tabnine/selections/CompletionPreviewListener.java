package com.tabnine.selections;

import com.intellij.openapi.editor.Editor;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.selection.SelectionRequest;
import com.tabnine.binary.requests.selection.SetStateBinaryRequest;
import com.tabnine.hover.HoverUpdater;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.state.UserState;
import com.tabnine.statusBar.StatusBarUpdater;
import java.util.function.Consumer;

public class CompletionPreviewListener {
  private final BinaryRequestFacade binaryRequestFacade;
  private final StatusBarUpdater statusBarUpdater;
  private final HoverUpdater hoverUpdater;

  public CompletionPreviewListener(
      BinaryRequestFacade binaryRequestFacade,
      StatusBarUpdater statusBarUpdater,
      HoverUpdater hoverUpdater) {
    this.binaryRequestFacade = binaryRequestFacade;
    this.statusBarUpdater = statusBarUpdater;
    this.hoverUpdater = hoverUpdater;
  }

  public void executeSelection(
      Editor editor,
      TabNineCompletion completion,
      String filename,
      Consumer<SelectionRequest> extendSelectionRequest) {
    SelectionRequest selection = new SelectionRequest();

    selection.language = SelectionUtil.asLanguage(filename);
    selection.netLength =
        completion.newPrefix.replaceFirst("^" + completion.oldPrefix, "").length();
    selection.linePrefixLength = completion.cursorPrefix.length();
    selection.lineNetPrefixLength = selection.linePrefixLength - completion.oldPrefix.length();
    selection.lineSuffixLength = completion.cursorSuffix.length();
    selection.origin = completion.origin;
    selection.length = completion.newPrefix.length();
    selection.strength = SelectionUtil.getStrength(completion);
    selection.completionKind = completion.completionKind;
    selection.snippetIntent = completion.snippet_intent;
    extendSelectionRequest.accept(selection);

    binaryRequestFacade.executeRequest(new SetStateBinaryRequest(selection));
    this.statusBarUpdater.updateStatusBar();
    this.hoverUpdater.update(editor);
    UserState.getInstance().getCompletionHintState().setIsCompletionHintShown(true);
  }
}
