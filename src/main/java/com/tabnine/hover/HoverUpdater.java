package com.tabnine.hover;

import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.util.Disposer;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.notifications.HoverBinaryRequest;
import com.tabnine.binary.requests.notifications.HoverBinaryResponse;
import com.tabnine.general.DependencyContainer;
import com.tabnine.intellij.completions.InlayHoverMouseMotionListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoverUpdater {
  private final BinaryRequestFacade binaryRequestFacade =
      DependencyContainer.instanceOfBinaryRequestFacade();

  public void update(final Editor editor) {
    final int caretOffset = editor.getCaretModel().getOffset();
    final AtomicReference<Inlay> inlayHolder = new AtomicReference<>();
    final AtomicBoolean documentChanged = new AtomicBoolean(false);
    addInlayDisposer(editor, inlayHolder, documentChanged);
    tryAddInlay(editor, caretOffset, inlayHolder, documentChanged);
  }

  private void tryAddInlay(
      Editor editor,
      int caretOffset,
      AtomicReference<Inlay> inlayHolder,
      AtomicBoolean documentChanged) {
    ApplicationManager.getApplication()
        .executeOnPooledThread(
            () -> {
              try {
                final Document document = editor.getDocument();
                // add the inlay at the end of the line. Ideally we would use
                // 'addAfterLineEndElement' but that's
                // only available from IJ > 191.
                final int currentLineNumber = document.getLineNumber(caretOffset);
                final int inlayOffset = document.getLineEndOffset(currentLineNumber);
                if (isInlayAlreadyDisplayed(editor, inlayOffset, currentLineNumber)) {
                  // inlay already displayed at the end of this line - don't add another one.
                  return;
                }
                handleInlayCreation(inlayHolder, documentChanged, editor, inlayOffset);
              } catch (Exception e) {
                if (e instanceof ControlFlowException) {
                  throw e;
                }
                Logger.getInstance(getClass()).warn("Error on locked item selection.", e);
              }
            });
  }

  private boolean isInlayAlreadyDisplayed(
      Editor editor, int inlayStartOffset, int currentLineNumber) {
    // search for our inlay between the current line end offset and the next line start offset.
    final int inlayEndOffset;
    if (currentLineNumber + 1 < editor.getDocument().getLineCount()) {
      inlayEndOffset = editor.getDocument().getLineEndOffset(currentLineNumber + 1);
    } else {
      inlayEndOffset = editor.getDocument().getTextLength(); // we're already at the last line.
    }
    return editor
        .getInlayModel()
        .getInlineElementsInRange(inlayStartOffset, inlayEndOffset)
        .stream()
        .anyMatch(s -> s.getRenderer() instanceof CustomHintRenderer && s.isValid());
  }

  private void handleInlayCreation(
      AtomicReference<Inlay> inlayHolder,
      AtomicBoolean documentChanged,
      Editor editor,
      int inlayOffset) {
    // get the inlay and hover popup data
    HoverBinaryResponse hoverBinaryResponse =
        this.binaryRequestFacade.executeRequest(new HoverBinaryRequest());
    if (hoverBinaryResponse == null
        || hoverBinaryResponse.getTitle() == null
        || documentChanged.get()) {
      return;
    }
    // inlay must be added from UI thread
    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              try {
                addInlay(editor, inlayOffset, inlayHolder, documentChanged, hoverBinaryResponse);
              } catch (Exception e) {
                if (e instanceof ControlFlowException) {
                  throw e;
                }
                Logger.getInstance(getClass()).warn("Error adding locked item inlay.", e);
              }
            });
  }

  private void addInlay(
      Editor editor,
      int inlayOffset,
      AtomicReference<Inlay> inlayHolder,
      AtomicBoolean documentChanged,
      HoverBinaryResponse hoverBinaryResponse) {
    synchronized (documentChanged) {
      if (documentChanged.get()) {
        return;
      }
      final Inlay inlay =
          editor
              .getInlayModel()
              .addInlineElement(
                  inlayOffset, true, new CustomHintRenderer(hoverBinaryResponse.getTitle()));
      if (inlay != null) {
        inlayHolder.set(inlay);
        editor.addEditorMouseMotionListener(
            new InlayHoverMouseMotionListener(this.binaryRequestFacade, hoverBinaryResponse, inlay),
            inlay);
      }
    }
  }

  /*
     Creates a document listener that removes the inlay (and popup, if visible) when the user continues to edit the
     doc (i.e. when the document changes).
  */
  private void addInlayDisposer(
      Editor editor, AtomicReference<Inlay> inlayHolder, AtomicBoolean documentChanged) {
    editor
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void documentChanged(@NotNull DocumentEvent event) {
                synchronized (documentChanged) { // sync to prevent race with inlay creation
                  documentChanged.set(true); // mark that doc has changed
                  if (inlayHolder.get() != null) {
                    Disposer.dispose(inlayHolder.get());
                  }
                  // doc changed, no need to keep listening
                  editor.getDocument().removeDocumentListener(this);
                }
              }
            });
  }

  // This is just a marker class to identify already displayed inlays.
  private static class CustomHintRenderer extends HintRenderer {

    public CustomHintRenderer(@Nullable String text) {
      super(text);
    }
  }
}
