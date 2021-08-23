package com.tabnine;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.tabnine.general.StaticConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TabnineRootFileType implements FileType {
    public static TabnineRootFileType INSTANCE = new TabnineRootFileType();
    @NotNull
    @Override
    public String getName() {
        return "Tabnine root file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Tabnine root file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "tabnine_root";
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
