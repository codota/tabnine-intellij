package com.tabnine;

import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.DocumentUtil;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.notifications.HoverBinaryRequest;
import com.tabnine.binary.requests.notifications.HoverBinaryResponse;
import com.tabnine.binary.requests.notifications.actions.HoverActionRequest;
import com.tabnine.general.DependencyContainer;
import com.tabnine.general.NotificationOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.tabnine.general.StaticConfig.ICON_AND_NAME;

public class LimitExceededLookupElement extends InsertNothingLookupElement {
    private final BinaryRequestFacade binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade();
    //URLs for adding the tabnine icon to the inlay popup.
    private final static String ICON_AND_NAME_URL;
    private final static String ICON_AND_NAME_DARK_URL;

    //This is a hack to get the light/dark icon URL for use in the popup html.
    static {
        ICON_AND_NAME_URL = ICON_AND_NAME.toString();
        final int suffixStartIndex = ICON_AND_NAME_URL.lastIndexOf(".");
        if (suffixStartIndex > 0) {
            ICON_AND_NAME_DARK_URL = ICON_AND_NAME_URL.substring(0, suffixStartIndex) + "_dark" +
                    ICON_AND_NAME_URL.substring(suffixStartIndex);
        } else {
            ICON_AND_NAME_DARK_URL = null;
        }
    }

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
        addDocumentListener(editor, inlayHolder, documentChanged);
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

    /*
        This listener removes the inlay (and popup, if visible) when the user continues to edit the doc
     */
    private void addDocumentListener(Editor editor, AtomicReference<Inlay> inlayHolder, AtomicBoolean documentChanged) {
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
            if (inlay == null) {
                return;
            } else {
                inlayHolder.set(inlay);
                editor.addEditorMouseMotionListener(new InlayHoverMouseMotionListener(
                        hoverBinaryResponse, inlay), inlay);
            }
        }
    }

    /*
        Opens the popup when the mouse hovers over the inlay.
     */
    private static class InlayHoverMouseMotionListener implements EditorMouseMotionListener {
        private final HoverBinaryResponse hoverBinaryResponse;
        private final Inlay inlay;
        private Balloon balloon;

        public InlayHoverMouseMotionListener(HoverBinaryResponse hoverBinaryResponse, Inlay inlay) {
            this.hoverBinaryResponse = hoverBinaryResponse;
            this.inlay = inlay;
        }

        @Override
        public void mouseMoved(@NotNull EditorMouseEvent e) {
            final MouseEvent mouseEvent = e.getMouseEvent();

            // Without this, hovering over the gutter can trigger events
            if (mouseEvent.getSource() != e.getEditor().getContentComponent()) {
                return;
            }
            final Point point = new Point(mouseEvent.getPoint());
            Rectangle inlayBounds = getInlayBounds(inlay, e.getEditor());
            if (inlayBounds == null) {
                return;
            }
            if (inlayBounds.contains(point)) {
                if (this.balloon == null) {
                    this.balloon = createPopup();
                    Disposer.register(
                        this.balloon,
                        () -> {
                            this.balloon.dispose();
                            this.balloon = null;
                        }
                    );
                    this.balloon.show(new RelativePoint(mouseEvent), Balloon.Position.atRight);
                }
            }
        }

        @NotNull
        private Balloon createPopup() {
            return JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
                buildHtmlContent(hoverBinaryResponse.getMessage()),
                null,
                JBColor.LIGHT_GRAY,
                new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent event) {
                        try {
                            if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED ||
                                event.getDescription() == null) {
                                return;
                            }
                            Optional<NotificationOption> selectedOption =
                                Arrays.stream(hoverBinaryResponse.getOptions()).filter(
                                    option -> option.getKey().equals(event.getDescription())).findFirst();
                            if (!selectedOption.isPresent()) {
                                Logger.getInstance(getClass()).warn(
                                    "Error activating option: " + event.getDescription()
                                            + ". No matching option found.");
                                return;
                            }
                            //call the binary to handle the clicked action.
                            ApplicationManager.getApplication().executeOnPooledThread(
                                () -> DependencyContainer.instanceOfBinaryRequestFacade()
                                        .executeRequest(new HoverActionRequest(
                                            hoverBinaryResponse.getId(),
                                            event.getDescription(),
                                            hoverBinaryResponse.getState(),
                                            hoverBinaryResponse.getMessage(),
                                            hoverBinaryResponse.getNotificationType(),
                                            selectedOption.get().getActions())
                                        )
                            );
                        } catch (Exception e) {
                            Logger.getInstance(getClass())
                                    .warn("Error handling locked inlay action.", e);
                        }
                    }
                }
            ).setDisposable(inlay).createBalloon();
        }

        /*
            This is a simple way to add the tabnine icon to the popup above the html content.
         */
        @Nullable
        private String buildHtmlContent(String message) {
            final String iconUrl =  EditorColorsManager.getInstance().isDarkEditor() ?
                    ICON_AND_NAME_DARK_URL : ICON_AND_NAME_URL;
            if (iconUrl == null) {
                return message;
            } else {
                return "<img src='" + iconUrl + "'> " + hoverBinaryResponse.getMessage();
            }
        }

        /*
            This code is copied from AfterLineEndInlayImpl since Inlay.getBounds method is only available
            from IJ > 183.
         */
        @Nullable
        private Rectangle getInlayBounds(Inlay inlay, Editor editor) {
            int targetOffset = DocumentUtil.getLineEndOffset(inlay.getOffset(), editor.getDocument());
            if (editor.getFoldingModel().isOffsetCollapsed(targetOffset)) return null;
            final Point pos = editor.visualPositionToXY(inlay.getVisualPosition());
            return new Rectangle(pos.x, pos.y, inlay.getWidthInPixels(), editor.getLineHeight());
        }
    }
}

