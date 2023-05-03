package com.tabnine;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.tabnineCommon.general.StaticConfig;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineIgnoreFileType implements FileType {
  public static TabnineIgnoreFileType INSANCE = new TabnineIgnoreFileType();

  @NotNull
  @Override
  public String getName() {
    return "Tabnine ignore file";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Tabnine ignore file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "tabnineignore";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return StaticConfig.ICON;
  }

  @Override
  public boolean isBinary() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Nullable
  @Override
  public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
    return null;
  }
}
