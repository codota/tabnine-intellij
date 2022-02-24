package com.tabnine.lifecycle;

import static com.tabnine.general.StaticConfig.TABNINE_PLUGIN_ID;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.uninstall.UninstallRequest;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class TabNinePluginStateListener implements PluginStateListener {
  // Temporarily disabling uninstall event due to a bug (24/02/22)
  private static final boolean DISABLE_UNINSTALL = true;
  private final UninstallReporter uninstallReporter;
  private final BinaryRequestFacade binaryRequestFacade;

  public TabNinePluginStateListener(
      UninstallReporter uninstallReporter, BinaryRequestFacade binaryRequestFacade) {
    this.uninstallReporter = uninstallReporter;
    this.binaryRequestFacade = binaryRequestFacade;
  }

  @Override
  public void install(@NotNull IdeaPluginDescriptor descriptor) {
    // Nothing
  }

  @Override
  public void uninstall(@NotNull IdeaPluginDescriptor descriptor) {
    if (DISABLE_UNINSTALL) {
      return;
    }

    Optional.ofNullable(descriptor.getPluginId())
        .filter(TABNINE_PLUGIN_ID::equals)
        .ifPresent(
            pluginId -> {
              if (binaryRequestFacade.executeRequest(new UninstallRequest()) == null) {
                uninstallReporter.reportUninstall(null);
              }
            });
  }
}
