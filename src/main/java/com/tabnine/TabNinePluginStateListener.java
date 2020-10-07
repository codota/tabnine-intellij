package com.tabnine;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.binary.BinaryRun;
import com.tabnine.binary.NoValidBinaryToRunException;
import com.tabnine.exceptions.TabNineDeadException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.tabnine.StaticConfig.TABNINE_PLUGIN_ID;
import static java.lang.String.format;

public class TabNinePluginStateListener implements PluginStateListener {
    private final BinaryRun binaryRun;

    public TabNinePluginStateListener(BinaryRun binaryRun) {
        this.binaryRun = binaryRun;
    }

    @Override
    public void install(@NotNull IdeaPluginDescriptor descriptor) {
        // Nothing
    }

    @Override
    public void uninstall(@NotNull IdeaPluginDescriptor descriptor) {
        Optional.ofNullable(descriptor.getPluginId()).filter(TABNINE_PLUGIN_ID::equals).ifPresent(pluginId -> {
            try {
                binaryRun.reportUninstall().waitFor();
            } catch (NoValidBinaryToRunException e) {
                Logger.getInstance(getClass()).warn("Couldn't find a binary to run", e);
            } catch (TabNineDeadException e) {
                Logger.getInstance(getClass()).warn(format("Couldn't communicate uninstalling to binary at: %s", e.getLocation()), e);
            } catch (InterruptedException e) {
                Logger.getInstance(getClass()).warn("Couldn't communicate uninstalling to binary.", e);
            }
        });
    }
}
