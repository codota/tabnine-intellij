package com.tabnine.lifecycle;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.tabnine.binary.TabNineGateway;
import com.tabnine.binary.exceptions.TabNineDeadException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.tabnine.general.StaticConfig.TABNINE_PLUGIN_ID;

public class TabNinePluginStateListener implements PluginStateListener {
    private final UninstallReporter uninstallReporter;
    private final TabNineGateway tabNineGateway;

    public TabNinePluginStateListener(UninstallReporter uninstallReporter, TabNineGateway tabNineGateway) {
        this.uninstallReporter = uninstallReporter;
        this.tabNineGateway = tabNineGateway;
    }

    @Override
    public void install(@NotNull IdeaPluginDescriptor descriptor) {
        // Nothing
    }

    @Override
    public void uninstall(@NotNull IdeaPluginDescriptor descriptor) {
        Optional.ofNullable(descriptor.getPluginId()).filter(TABNINE_PLUGIN_ID::equals).ifPresent(pluginId -> {
            try {
                tabNineGateway.request(new UninstallRequest());
            } catch (TabNineDeadException e) {
                uninstallReporter.reportUninstall();
            }
        });
    }
}
