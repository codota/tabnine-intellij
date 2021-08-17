package com.tabnine.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TabnineSettingsConfigurable implements Configurable {

  private TabnineSettingsComponent settingsComponent;

  @Override
  public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
    return "Tabnine Settings";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return settingsComponent.getPreferredFocusedComponent();
  }

  @Override
  public @Nullable JComponent createComponent() {
    settingsComponent = new TabnineSettingsComponent();
    return settingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    TabnineSettingsState settings = TabnineSettingsState.getInstance();
    return settingsComponent.shouldUseTabKeyForInlineCompletions()
        != settings.useTabKeyForInlineCompletions;
  }

  @Override
  public void apply() throws ConfigurationException {
    TabnineSettingsState settings = TabnineSettingsState.getInstance();
    settings.useTabKeyForInlineCompletions = settingsComponent.shouldUseTabKeyForInlineCompletions();
  }

  @Override
  public void reset() {
    TabnineSettingsState settings = TabnineSettingsState.getInstance();
    settingsComponent.setUseTabKeyForInlineCompletions(settings.useTabKeyForInlineCompletions);
  }

  @Override
  public void disposeUIResources() {
    settingsComponent = null;
  }
}
