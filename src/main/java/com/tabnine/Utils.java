package com.tabnine;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.util.Producer;

import java.util.Arrays;
import java.util.Optional;

public final class Utils {
    private static final String UNKNOWN = "Unknown";

    private static PluginId getPluginId() {
        return PluginManager.getPluginByClassName(TabNineProcess.class.getName());
    }

    public static String getPluginVersion() {
        return Optional.ofNullable(getPluginId())
                .flatMap(pluginId ->
                        Arrays.stream(PluginManager.getPlugins())
                                .filter(plugin -> plugin.getPluginId() == pluginId)
                                .map(IdeaPluginDescriptor::getVersion)
                                .findAny())
                .orElse(UNKNOWN);
    }

    public static <T> Optional<T> emptyUponException(Producer<T> content) {
        try {
            return Optional.of(content.produce());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
