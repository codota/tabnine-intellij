package com.tabnine;

import com.tabnine.state.UninstallReporter;

import static com.tabnine.general.Utils.getTabNinePluginDescriptor;

public class TabNineDisablePluginListener {
    private final UninstallReporter uninstallReporter;
    private boolean isDisabled;

    public TabNineDisablePluginListener(UninstallReporter uninstallReporter) {
        this.uninstallReporter = uninstallReporter;
        this.isDisabled = pluginIsDisabled();
    }

    public void onDisable() {
        if(this.isDisabled) {
            return;
        }

        if(pluginIsDisabled()) {
            this.isDisabled = true;
            uninstallReporter.reportUninstall("disable=true");
        }
    }

    private static boolean pluginIsDisabled() {
        return getTabNinePluginDescriptor().map(plugin -> !plugin.isEnabled()).orElse(false);
    }
}
