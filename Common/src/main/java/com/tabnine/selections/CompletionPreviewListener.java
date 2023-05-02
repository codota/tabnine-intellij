package com.tabnine.selections;

import com.intellij.openapi.editor.Editor;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.selection.SelectionRequest;
import com.tabnine.binary.requests.selection.SetStateBinaryRequest;
import com.tabnine.capabilities.RenderingMode;
import com.tabnine.hover.HoverUpdater;
import com.tabnine.prediction.TabNineCompletion;
import java.util.function.Consumer;

public class CompletionPreviewListener {
  private final BinaryRequestFacade binaryRequestFacade;
  private final HoverUpdater hoverUpdater;

  public CompletionPreviewListener(
      BinaryRequestFacade binaryRequestFacade, HoverUpdater hoverUpdater) {
    this.binaryRequestFacade = binaryRequestFacade;
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
    this.hoverUpdater.update(editor);
    CompletionObserver.notifyListeners();
  }
}
