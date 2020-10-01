package com.tabnine;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.TextRange;
import com.tabnine.binary.TabNineGateway;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public final class Utils {
    private static final String UNKNOWN = "Unknown";

    public static String getPluginVersion() {
        return Optional.ofNullable(getPluginId())
                .flatMap(pluginId ->
                        Arrays.stream(PluginManager.getPlugins())
                                .filter(plugin -> plugin.getPluginId() == pluginId)
                                .map(IdeaPluginDescriptor::getVersion)
                                .findAny())
                .orElse(UNKNOWN);
    }

    private static PluginId getPluginId() {
        return PluginManager.getPluginByClassName(TabNineGateway.class.getName());
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

    @NotNull
    public static String readContent(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString(StandardCharsets.UTF_8.name()).trim();
    }
}
