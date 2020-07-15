package com.tabnine.lifecycle;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.tabnine.TabNineProcess;
import com.tabnine.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;

public class TabNinePluginStateListener implements PluginStateListener {

    @Override
    public void install(@NotNull IdeaPluginDescriptor descriptor) {
    }

    @Override
    public void uninstall(@NotNull IdeaPluginDescriptor descriptor) {
        final PluginId pluginId = descriptor.getPluginId();
        if (pluginId != null && pluginId.equals(Utils.getPluginId())) {
            try {
                TabNineProcess.startTabNine(true, Collections.singletonList("--uninstalling"));
            } catch (IOException e) {
                Logger.getInstance(getClass()).warn("Error handling TabNine uninstalling:", e);
            }
        }
    }
}
