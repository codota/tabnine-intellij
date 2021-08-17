package com.tabnine.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

public class TabnineSettingsComponent {
  private final JPanel mainPanel;
  private final JBCheckBox useTabKey =
      new JBCheckBox("<html>Use <b>Tab</b> to apply inline completions");

  public TabnineSettingsComponent() {
    mainPanel =
        FormBuilder.createFormBuilder()
            .addComponent(useTabKey, 1)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  public JPanel getPanel() {
    return mainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return useTabKey;
  }

  public boolean shouldUseTabKeyForInlineCompletions() {
    return useTabKey.isSelected();
  }

  public void setUseTabKeyForInlineCompletions(boolean newState) {
    this.useTabKey.setSelected(newState);
  }
}
