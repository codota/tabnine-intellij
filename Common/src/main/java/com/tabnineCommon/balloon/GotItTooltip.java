package com.tabnineCommon.balloon;

import static com.tabnineCommon.general.StaticConfig.ICON;
import static com.tabnineCommon.general.Utils.wrapWithHtml;
import static com.tabnineCommon.general.Utils.wrapWithHtmlTag;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.notifications.shown.HintShownRequest;
import com.tabnineCommon.general.IProviderOfThings;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public class GotItTooltip implements Disposable {
  private final String tooltipId;
  private final String tooltipHeader;
  private final String tooltipBody;
  private final GotItTooltipAction gotItTooltipAction;
  private boolean isVisible = false;
  private Balloon tooltip;
  private static final int POINT_DX = -5;
  private static final int POINT_DY_DENOMINATOR = 2;

  public GotItTooltip(
      String tooltipId,
      String tooltipHeader,
      String tooltipBody,
      GotItTooltipAction gotItTooltipAction) {
    this.tooltipId = tooltipId;
    this.tooltipHeader = wrapWithHtml(wrapWithHtmlTag(tooltipHeader, "h3"));
    this.tooltipBody = wrapWithHtml(tooltipBody);
    this.gotItTooltipAction = gotItTooltipAction;
  }

  public void show(Editor editor) {
    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              this.isVisible = true;
              BinaryRequestFacade binaryRequestFacade =
                  ServiceManager.getService(IProviderOfThings.class).getBinaryRequestFacade();
              JButton gotItButton = new JButton("Got It");
              tooltip =
                  createBalloon(createTooltipContent(gotItButton, tooltipHeader, tooltipBody));
              tooltip.addListener(
                  new JBPopupListener() {
                    @Override
                    public void beforeShown(@NotNull LightweightWindowEvent event) {
                      binaryRequestFacade.executeRequest(
                          new HintShownRequest(tooltipId, tooltipHeader, null, null));
                    }
                  });
              showTooltip(editor, tooltip);
              gotItButton.addActionListener(
                  e -> {
                    gotItTooltipAction.onGotItClicked();
                    dispose();
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

  private void showTooltip(Editor editor, Balloon tooltip) {
    RelativePoint relativePoint = JBPopupFactory.getInstance().guessBestPopupLocation(editor);
    relativePoint.getPoint().translate(POINT_DX, -editor.getLineHeight() / POINT_DY_DENOMINATOR);
    tooltip.show(relativePoint, Balloon.Position.atLeft);
  }

  private Balloon createBalloon(JPanel content) {
    return JBPopupFactory.getInstance()
        .createBalloonBuilder(content)
        .setBorderInsets(JBUI.insets(5, 15, 10, 15))
        .setFillColor(JBColor.background())
        .setHideOnKeyOutside(false)
        .createBalloon();
  }

  public boolean isVisible() {
    return isVisible;
  }

  @Override
  public void dispose() {
    if (tooltip != null) {
      Disposer.dispose(tooltip);
      isVisible = false;
    }
  }
}
