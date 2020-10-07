package com.tabnine;

import com.intellij.ide.plugins.PluginStateListener;

import static com.tabnine.Utils.getTabNinePluginDescriptor;

public class TabNineDisablePluginListener {
    private final PluginStateListener pluginStateListener;
    private boolean isDisabled;

    public TabNineDisablePluginListener(PluginStateListener pluginStateListener) {
        this.pluginStateListener = pluginStateListener;
        this.isDisabled = pluginIsDisabled();
    }

    public void onDisable() {
        if(this.isDisabled) {
            return;
        }

        if(pluginIsDisabled()) {
            this.isDisabled = true;
            pluginStateListener.uninstall(getTabNinePluginDescriptor().get());
        }
    }

    private static boolean pluginIsDisabled() {
        return getTabNinePluginDescriptor().map(plugin -> !plugin.isEnabled()).orElse(false);
    }
}
