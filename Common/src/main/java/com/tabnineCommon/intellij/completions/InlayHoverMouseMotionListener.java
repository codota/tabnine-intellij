package com.tabnineCommon.intellij.completions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.DocumentUtil;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.notifications.HoverBinaryResponse;
import com.tabnineCommon.binary.requests.notifications.actions.HoverActionRequest;
import com.tabnineCommon.binary.requests.notifications.shown.HoverShownRequest;
import com.tabnineCommon.general.DependencyContainer;
import com.tabnineCommon.general.NotificationOption;
import com.tabnineCommon.general.StaticConfig;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InlayHoverMouseMotionListener implements EditorMouseMotionListener {
  private final BinaryRequestFacade binaryRequestFacade;
  // URLs for adding the tabnine icon to the inlay popup.
  private static final String ICON_AND_NAME_URL;
  private static final String ICON_AND_NAME_DARK_URL;

  private final HoverBinaryResponse hoverBinaryResponse;
  private final Inlay inlay;
  private Balloon balloon;

  // This is a hack to get the light/dark icon URL for use in the popup html.
  static {
    ICON_AND_NAME_URL =
        "jar:"
            + LimitExceededLookupElement.class
                .getClassLoader()
                .getResource(StaticConfig.ICON_AND_NAME_PATH)
                .getPath();
    final int suffixStartIndex = ICON_AND_NAME_URL.lastIndexOf(".");
    if (suffixStartIndex > 0) {
      ICON_AND_NAME_DARK_URL =
          ICON_AND_NAME_URL.substring(0, suffixStartIndex)
              + "_dark"
              + ICON_AND_NAME_URL.substring(suffixStartIndex);
    } else {
      ICON_AND_NAME_DARK_URL = null;
    }
  }

  public InlayHoverMouseMotionListener(
      BinaryRequestFacade binaryRequestFacade,
      HoverBinaryResponse hoverBinaryResponse,
      Inlay inlay) {
    this.binaryRequestFacade = binaryRequestFacade;
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
            });
        this.balloon.show(new RelativePoint(mouseEvent), Balloon.Position.atRight);
      }
    }
  }

  @NotNull
  private Balloon createPopup() {
    sendHoverShownRequest();
    return JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(
            buildHtmlContent(hoverBinaryResponse.getMessage()),
            null,
            null,
            MessageType.INFO.getPopupBackground(),
            new HyperlinkListener() {
              @Override
              public void hyperlinkUpdate(HyperlinkEvent event) {
                try {
                  if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED
                      || event.getDescription() == null) {
                    return;
                  }
                  Optional<NotificationOption> selectedOption =
                      Arrays.stream(hoverBinaryResponse.getOptions())
                          .filter(option -> option.getKey().equals(event.getDescription()))
                          .findFirst();
                  if (!selectedOption.isPresent()) {
                    Logger.getInstance(getClass())
                        .warn(
                            "Error activating option: "
                                + event.getDescription()
                                + ". No matching option found.");
                    return;
                  }
                  // call the binary to handle the clicked action.
                  ApplicationManager.getApplication()
                      .executeOnPooledThread(
                          () ->
                              DependencyContainer.instanceOfBinaryRequestFacade()
                                  .executeRequest(
                                      new HoverActionRequest(
                                          hoverBinaryResponse.getId(),
                                          event.getDescription(),
                                          hoverBinaryResponse.getState(),
                                          hoverBinaryResponse.getMessage(),
                                          hoverBinaryResponse.getNotificationType(),
                                          selectedOption.get().getActions())));
                } catch (Exception e) {
                  Logger.getInstance(getClass()).warn("Error handling locked inlay action.", e);
                }
              }
            })
        .setDisposable(inlay)
        .createBalloon();
  }

  /*
     This is a simple way to add the tabnine icon to the popup above the html content.
  */
  @Nullable
  private String buildHtmlContent(String message) {
    final String iconUrl =
        EditorColorsManager.getInstance().isDarkEditor()
            ? ICON_AND_NAME_DARK_URL
            : ICON_AND_NAME_URL;
    if (iconUrl == null) {
      return message;
    } else {
      return "<img src='" + iconUrl + "'> " + hoverBinaryResponse.getMessage();
    }
  }

  private void sendHoverShownRequest() {
    ApplicationManager.getApplication()
        .executeOnPooledThread(
            () -> {
              try {
                this.binaryRequestFacade.executeRequest(
                    new HoverShownRequest(
                        this.hoverBinaryResponse.getId(),
                        this.hoverBinaryResponse.getMessage(),
                        this.hoverBinaryResponse.getNotificationType(),
                        this.hoverBinaryResponse.getState()));
              } catch (RuntimeException e) {
                // swallow - nothing to do with this
              }
            });
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
