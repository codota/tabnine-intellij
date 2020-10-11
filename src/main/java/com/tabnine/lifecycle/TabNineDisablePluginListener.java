package com.tabnine.lifecycle;

import com.tabnine.binary.TabNineGateway;
import com.tabnine.binary.exceptions.TabNineDeadException;

import static com.tabnine.general.Utils.getTabNinePluginDescriptor;

public class TabNineDisablePluginListener {
    private final UninstallReporter uninstallReporter;
    private final TabNineGateway tabNineGateway;
    private boolean isDisabled;

    public TabNineDisablePluginListener(UninstallReporter uninstallReporter, TabNineGateway tabNineGateway) {
        this.uninstallReporter = uninstallReporter;
        this.tabNineGateway = tabNineGateway;
        this.isDisabled = pluginIsDisabled();
    }

    public void onDisable() {
        if(this.isDisabled) {
            return;
        }

        if(pluginIsDisabled()) {
            this.isDisabled = true;
            try {
                tabNineGateway.request(new UninstallRequest());
            } catch (TabNineDeadException e) {
                uninstallReporter.reportUninstall("disable=true");
            }
        } else {
            this.isDisabled = false;
        }
    }

    private static boolean pluginIsDisabled() {
        return getTabNinePluginDescriptor().map(plugin -> !plugin.isEnabled()).orElse(false);
    }
}
