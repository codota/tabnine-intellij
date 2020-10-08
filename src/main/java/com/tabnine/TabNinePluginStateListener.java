package com.tabnine;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.tabnine.state.UninstallReporter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.tabnine.general.StaticConfig.TABNINE_PLUGIN_ID;

public class TabNinePluginStateListener implements PluginStateListener {
    private final UninstallReporter uninstallReporter;

    public TabNinePluginStateListener(UninstallReporter uninstallReporter) {
        this.uninstallReporter = uninstallReporter;
    }

    @Override
    public void install(@NotNull IdeaPluginDescriptor descriptor) {
        // Nothing
    }

    @Override
    public void uninstall(@NotNull IdeaPluginDescriptor descriptor) {
        Optional.ofNullable(descriptor.getPluginId()).filter(TABNINE_PLUGIN_ID::equals).ifPresent(pluginId -> {
            uninstallReporter.reportUninstall();
        });
    }
}
