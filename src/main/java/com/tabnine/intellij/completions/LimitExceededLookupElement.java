package com.tabnine.intellij.completions;

import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEvent;
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
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is a lookup element for locked completions. Locked completions are completions that appear when the user
 * already exceeded the completions daily quota. The completion text appears as usual and its type text contains a
 * lock symobl. When selecting such a completion no text is inserted and instead, a gray inlay is shown, with a message
 * indicating the quota was exceeded. Hovering the mouse over this inlay open a balloon with more details and possibly
 * CTA links.
 */
public class LimitExceededLookupElement extends InsertNothingLookupElement {
    private final BinaryRequestFacade binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade();

    protected LimitExceededLookupElement(LookupElement delegate, String prefix) {
        super(delegate, prefix);
    }

    @Override
    public void itemSelected(@NotNull LookupEvent lookupEvent) {
        if (lookupEvent.getItem() != this) {
            return; //handle only selection of this item.
        }
        final Editor editor = lookupEvent.getLookup().getEditor();
        final int caretOffset = editor.getCaretModel().getOffset();
        final AtomicReference<Inlay> inlayHolder = new AtomicReference<>();
        final AtomicBoolean documentChanged = new AtomicBoolean(false);
        addInlayDisposer(editor, inlayHolder, documentChanged);
        tryAddLimitExceededInlay(editor, caretOffset, inlayHolder, documentChanged);
    }

    private void tryAddLimitExceededInlay(Editor editor, int caretOffset, AtomicReference<Inlay> inlayHolder, AtomicBoolean documentChanged) {
        ApplicationManager.getApplication().executeOnPooledThread(
            () -> {
                try {
                    handleInlayCreation(inlayHolder, documentChanged,
                            editor, caretOffset);
                } catch(Exception e) {
                    if (e instanceof ControlFlowException) {
                        throw e;
                    }
                    Logger.getInstance(getClass()).warn("Error on locked item selection.", e);
                }
            }
        );
    }

    private void handleInlayCreation(AtomicReference<Inlay> inlayHolder, AtomicBoolean documentChanged,
                                    Editor editor, int caretOffset) {
        //get the inlay and hover popup data
        HoverBinaryResponse hoverBinaryResponse =
                this.binaryRequestFacade.executeRequest(new HoverBinaryRequest());
        if (hoverBinaryResponse == null || hoverBinaryResponse.getTitle() == null ||
                documentChanged.get()) {
            return;
        }
        //inlay must be added from UI thread
        ApplicationManager.getApplication().invokeLater(
                () -> {
                    try {
                        addInlay(editor, caretOffset, inlayHolder, documentChanged, hoverBinaryResponse);
                    } catch(Exception e) {
                        if (e instanceof ControlFlowException) {
                            throw e;
                        }
                        Logger.getInstance(getClass()).warn("Error adding locked item inlay.", e);
                    }
                }
        );
    }

    private void addInlay(Editor editor, int caretOffset, AtomicReference<Inlay> inlayHolder,
                          AtomicBoolean documentChanged, HoverBinaryResponse hoverBinaryResponse) {
        synchronized(documentChanged) {
            if (documentChanged.get()) {
                return;
            }
            final Document document = editor.getDocument();
            //add the inlay at the end of the line. Ideally we would use 'addAfterLineEndElement' but that's
            //only available from IJ > 191.
            final int inlayOffset = document.getLineEndOffset(document.getLineNumber(caretOffset));
            final Inlay inlay = editor.getInlayModel().addInlineElement(
                    inlayOffset,
                    true,
                    new HintRenderer(hoverBinaryResponse.getTitle())
            );
            if (inlay != null) {
                inlayHolder.set(inlay);
                editor.addEditorMouseMotionListener(new InlayHoverMouseMotionListener(
                        hoverBinaryResponse, inlay), inlay);
            }
        }
    }

    /*
        Creates a document listener that removes the inlay (and popup, if visible) when the user continues to edit the
        doc (i.e. when the document changes).
     */
    private void addInlayDisposer(Editor editor, AtomicReference<Inlay> inlayHolder, AtomicBoolean documentChanged) {
        editor.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void documentChanged(@NotNull DocumentEvent event) {
                        synchronized (documentChanged) {//sync to prevent race with inlay creation
                            documentChanged.set(true); //mark that doc has changed
                            if (inlayHolder.get() != null) {
                                Disposer.dispose(inlayHolder.get());
                            }
                            // doc changed, no need to keep listening
                            editor.getDocument().removeDocumentListener(this);
                        }
                    }
                }
        );
    }

}

