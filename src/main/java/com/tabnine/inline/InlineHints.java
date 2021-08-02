package com.tabnine.inline;

import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.tabnine.general.StaticConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class InlineHints {

  private static final HintManagerImpl hintManager = HintManagerImpl.getInstanceImpl();
  private static String currentText;
  private static JComponent preInsertionHintComponent;

  static {
    initPreInsertionHint();
  }

  private static void initPreInsertionHint() {
    String nextShortcut =
        KeymapUtil.getFirstKeyboardShortcutText(
            ActionManager.getInstance().getAction(ShowNextInlineCompletionAction.ACTION_ID));
    String prevShortcut =
        KeymapUtil.getFirstKeyboardShortcutText(
            ActionManager.getInstance().getAction(ShowPreviousInlineCompletionAction.ACTION_ID));
    String acceptShortcut = KeymapUtil.getKeyText(KeyEvent.VK_RIGHT);
    String cancelShortcut = KeymapUtil.getKeyText(KeyEvent.VK_ESCAPE);
    String text =
        "Next ("
            + nextShortcut
            + ") Prev ("
            + prevShortcut
            + ") Accept ("
            + acceptShortcut
            + ") Cancel ("
            + cancelShortcut
            + ")";
    if (!text.equals(currentText) || preInsertionHintComponent == null) {
      currentText = text;
      preInsertionHintComponent = createInlineHintComponent(text);
    }
  }

  private static JComponent createInlineHintComponent(String text) {
    SimpleColoredComponent component = HintUtil.createInformationComponent();
    component.setIconOnTheRight(true);
    component.setIcon(StaticConfig.ICON_AND_NAME);
    SimpleColoredText coloredText =
        new SimpleColoredText(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    coloredText.appendToComponent(component);
    return new InlineHintLabel(component);
  }

  public static boolean showPreInsertionHint(@NotNull Editor editor) {
    try {
      initPreInsertionHint();
      if (preInsertionHintComponent == null) {
        System.out.println("Whatttt? preInsertionHintComponent is null");
      }
      hintManager.showInformationHint(editor, preInsertionHintComponent);
      return true;
    } catch (Throwable e) {
      Logger.getInstance(InlineHints.class).warn("showPreInsertionHint failed", e);
      return false;
    }
  }

  private static class InlineHintLabel extends JPanel {

    private JEditorPane myPane;
    private SimpleColoredComponent myColoredComponent;

    private InlineHintLabel(@NotNull SimpleColoredComponent component) {
      super();
      setLayout(new BorderLayout());
      setText(component);
    }

    private void setText(@NotNull SimpleColoredComponent colored) {
      clearText();
      myColoredComponent = colored;
      add(myColoredComponent, BorderLayout.CENTER);
      setOpaque(true);
      setBackground(colored.getBackground());
      revalidate();
      repaint();
    }

    private void clearText() {
      if (myPane != null) {
        remove(myPane);
        myPane = null;
      }

      if (myColoredComponent != null) {
        remove(myColoredComponent);
        myColoredComponent = null;
      }
    }

    @Override
    public boolean requestFocusInWindow() {
      if (myPane != null) {
        return myPane.requestFocusInWindow();
      } else if (myColoredComponent != null) {
        return myColoredComponent.requestFocusInWindow();
      }
      return super.requestFocusInWindow();
    }
  }
}
