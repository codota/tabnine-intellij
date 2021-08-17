package com.tabnine.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;

@State(name = "com.tabnine.settings.TabnineSettingsState", storages = @Storage("tabnine.xml"))
public class TabnineSettingsState implements PersistentStateComponent<TabnineSettingsState> {

  public boolean useTabKeyForInlineCompletions = false;

  public static TabnineSettingsState getInstance() {
    return ServiceManager.getService(TabnineSettingsState.class);
  }

  @Override
  public @Nullable TabnineSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull TabnineSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public int getInlineCompletionApplyKeyCode() {
    return useTabKeyForInlineCompletions ? KeyEvent.VK_TAB : KeyEvent.VK_RIGHT;
  }
}
