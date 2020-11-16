package com.tabnine.lifecycle;

import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.uninstall.UninstallRequest;

import static com.tabnine.general.Utils.getTabNinePluginDescriptor;
import static java.util.Collections.singletonMap;

public class TabNineDisablePluginListener {
    private final UninstallReporter uninstallReporter;
    private final BinaryRequestFacade binaryRequestFacade;
    private boolean isDisabled;

    public TabNineDisablePluginListener(UninstallReporter uninstallReporter, BinaryRequestFacade binaryRequestFacade) {
        this.uninstallReporter = uninstallReporter;
        this.binaryRequestFacade = binaryRequestFacade;
        this.isDisabled = pluginIsDisabled();
    }

    private static boolean pluginIsDisabled() {
        return getTabNinePluginDescriptor().map(plugin -> !plugin.isEnabled()).orElse(false);
    }

    public void onDisable() {
        if (this.isDisabled) {
            return;
        }

        if (pluginIsDisabled()) {
            this.isDisabled = true;

            if (binaryRequestFacade.executeRequest(new UninstallRequest()) == null) {
                uninstallReporter.reportUninstall(singletonMap("disable", true));
            }
        } else {
            this.isDisabled = false;
        }
    }
}
