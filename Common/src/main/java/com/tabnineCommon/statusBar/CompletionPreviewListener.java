package com.tabnineCommon.statusBar;

import com.intellij.openapi.editor.Editor;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.selection.SelectionRequest;
import com.tabnineCommon.binary.requests.selection.SetStateBinaryRequest;
import com.tabnineCommon.capabilities.RenderingMode;
import com.tabnineCommon.hover.HoverUpdater;
import com.tabnineCommon.prediction.TabNineCompletion;
import com.tabnineCommon.selections.CompletionObserver;
import com.tabnineCommon.selections.SelectionUtil;
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
      RenderingMode renderingMode,
      Consumer<SelectionRequest> extendSelectionRequest) {
    SelectionRequest selection = new SelectionRequest();

    selection.language = SelectionUtil.asLanguage(filename);
    selection.netLength =
        completion.newPrefix.replaceFirst("^" + completion.oldPrefix, "").length();
    selection.linePrefixLength = completion.cursorPrefix.length();
    selection.lineNetPrefixLength = selection.linePrefixLength - completion.oldPrefix.length();
    selection.lineSuffixLength = completion.cursorSuffix.length();
    selection.origin =
        completion.completionMetadata != null ? completion.completionMetadata.getOrigin() : null;
    selection.length = completion.newPrefix.length();
    selection.strength = SelectionUtil.getStrength(completion);
    selection.completionKind =
        completion.completionMetadata != null
            ? completion.completionMetadata.getCompletion_kind()
            : null;
    selection.snippetContext =
        completion.completionMetadata != null
            ? completion.completionMetadata.getSnippet_context()
            : null;
    selection.suggestionRenderingMode = renderingMode;
    selection.suggestionTrigger = completion.suggestionTrigger;
    extendSelectionRequest.accept(selection);

    binaryRequestFacade.executeRequest(new SetStateBinaryRequest(selection));
    this.statusBarUpdater.updateStatusBar();
    this.hoverUpdater.update(editor);
    CompletionObserver.notifyListeners();
  }
}
