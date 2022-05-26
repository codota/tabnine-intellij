package com.tabnine.selections;

import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.selection.SelectionRequest;
import com.tabnine.binary.requests.selection.SetStateBinaryRequest;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.statusBar.StatusBarUpdater;
import java.util.function.Consumer;

public class CompletionPreviewListener {
  private final BinaryRequestFacade binaryRequestFacade;
  private final StatusBarUpdater statusBarUpdater;

  public CompletionPreviewListener(
      BinaryRequestFacade binaryRequestFacade, StatusBarUpdater statusBarUpdater) {
    this.binaryRequestFacade = binaryRequestFacade;
    this.statusBarUpdater = statusBarUpdater;
  }

  public void executeSelection(
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
  }
}
