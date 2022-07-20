package com.tabnine.balloon;

import static com.tabnine.general.DependencyContainer.instanceOfBinaryRequestFacade;
import static com.tabnine.general.StaticConfig.ICON;
import static com.tabnine.general.Utils.wrapWithHtml;
import static com.tabnine.general.Utils.wrapWithHtmlTag;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.notifications.shown.HintShownRequest;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public class GotItTooltip {
  private final String tooltipId;
  private final String tooltipHeader;
  private final String tooltipBody;
  private final GotItTooltipActions gotItTooltipActions;

  public GotItTooltip(
      String tooltipId,
      String tooltipHeader,
      String tooltipBody,
      GotItTooltipActions gotItTooltipActions) {
    this.tooltipId = tooltipId;
    this.tooltipHeader = wrapWithHtml(wrapWithHtmlTag(tooltipHeader, "h3"));
    this.tooltipBody = wrapWithHtml(tooltipBody);
    this.gotItTooltipActions = gotItTooltipActions;
  }

  public void show(Editor editor) {
    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();
              JButton gotItButton = new JButton("Got It");
              Balloon tooltip =
                  createBalloon(createTooltipContent(gotItButton, tooltipHeader, tooltipBody));
              tooltip.addListener(
                  new JBPopupListener() {
                    @Override
                    public void beforeShown(@NotNull LightweightWindowEvent event) {
                      binaryRequestFacade.executeRequest(
                          new HintShownRequest(tooltipId, tooltipHeader, null, null));
                    }
                  });
              addTooltipDisposer(editor, tooltip);
              showTooltip(editor, tooltip);
              gotItButton.addActionListener(
                  e -> {
                    gotItTooltipActions.onGotItClicked();
                    Disposer.dispose(tooltip);
                  });
            });
  }

  private JPanel createTooltipContent(
      JButton gotItButton, String tooltipHeader, String tooltipBody) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(JBColor.background());

    JLabel title = new JLabel();
    title.setIcon(ICON);
    title.setText(tooltipHeader);
    panel.add(title);

    JLabel description = new JLabel();
    description.setText(tooltipBody);
    panel.add(description);

    panel.add(Box.createVerticalStrut(15));
    panel.add(gotItButton);

    return panel;
  }

  private void addTooltipDisposer(Editor editor, Balloon tooltip) {
    editor
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void documentChanged(@NotNull DocumentEvent event) {
                Disposer.dispose(tooltip);
                editor.getDocument().removeDocumentListener(this);
              }
            });
  }

  private void showTooltip(Editor editor, Balloon tooltip) {
    RelativePoint relativePoint = JBPopupFactory.getInstance().guessBestPopupLocation(editor);
    relativePoint.getPoint().translate(-5, -editor.getLineHeight() / 2);
    tooltip.show(relativePoint, Balloon.Position.atLeft);
  }

  private Balloon createBalloon(JPanel content) {
    return JBPopupFactory.getInstance()
        .createBalloonBuilder(content)
        .setBorderInsets(JBUI.insets(5, 15, 10, 15))
        .setFillColor(JBColor.background())
        .createBalloon();
  }
}
