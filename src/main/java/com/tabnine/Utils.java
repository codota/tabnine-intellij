package com.tabnine;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.TextRange;
import com.tabnine.binary.TabNineProcess;

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

    public static boolean endsWithADot(Document doc, int pos) {
        int begin = pos - ".".length();
        if (begin < 0 || pos > doc.getTextLength()) {
            return false;
        } else {
            String tail = doc.getText(new TextRange(begin, pos));
            return tail.equals(".");
        }
    }
}
