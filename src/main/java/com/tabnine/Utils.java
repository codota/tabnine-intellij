package com.tabnine;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;

public final class Utils {
    private static final String UNKNOWN = "Unknown";

    public static PluginId getPluginId() {
        return PluginManager.getPluginByClassName(TabNineProcess.class.getName());
    }

    public static String getPluginVersion() {
        PluginId pluginId = getPluginId();
        String pluginVersion = UNKNOWN;
        if (pluginId != null) {
            for (IdeaPluginDescriptor plugin : PluginManager.getPlugins()) {
                if (pluginId == plugin.getPluginId()) {
                    pluginVersion = plugin.getVersion();
                    break;
                }
            }
        }
        return pluginVersion;
    }
}
