package com.tabnineCommon.binary;

import static com.tabnineCommon.general.StaticConfig.*;
import static com.tabnineCommon.general.Utils.cmdSanitize;
import static com.tabnineCommon.general.Utils.getTabNinePluginVersion;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PermanentInstallationID;
import com.intellij.util.PlatformUtils;
import com.tabnineCommon.binary.exceptions.NoValidBinaryToRunException;
import com.tabnineCommon.binary.exceptions.TabNineDeadException;
import com.tabnineCommon.binary.fetch.BinaryVersionFetcher;
import com.tabnineCommon.config.Config;
import com.tabnineCommon.general.StaticConfig;
import com.tabnineCommon.general.Utils;
import com.tabnineCommon.inline.DebounceUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BinaryRun {
  private final BinaryVersionFetcher binaryFetcher;

  public BinaryRun(BinaryVersionFetcher binaryFetcher) {
    this.binaryFetcher = binaryFetcher;
  }

  @NotNull
  public List<String> generateRunCommand(@Nullable Map<String, Object> additionalMetadata)
      throws NoValidBinaryToRunException {
    List<String> command = new ArrayList<>(singletonList(binaryFetcher.fetchBinary()));

    command.addAll(getBinaryConstantParameters(additionalMetadata));

    return command;
  }

  public Process reportUninstall(@Nullable Map<String, Object> additionalMetadata)
      throws NoValidBinaryToRunException, TabNineDeadException {
    String fullLocation = binaryFetcher.fetchBinary();
    List<String> command = new ArrayList<>(asList(fullLocation, UNINSTALLING_FLAG));

    command.addAll(getBinaryConstantParameters(additionalMetadata));

    try {
      return new ProcessBuilder(command).start();
    } catch (IOException e) {
      throw new TabNineDeadException(e, fullLocation);
    }
  }

  private ArrayList<String> getBinaryConstantParameters(
      @Nullable Map<String, Object> additionalMetadata) {
    ArrayList<String> constantParameters = new ArrayList<>();
    if (ApplicationManager.getApplication() != null
        && !ApplicationManager.getApplication().isUnitTestMode()) {
      List<String> metadata =
          new ArrayList<>(
              asList(
                  "--client-metadata",
                  "pluginVersion=" + cmdSanitize(getTabNinePluginVersion()),
                  "clientIsUltimate=" + PlatformUtils.isIdeaUltimate(),
                  "clientChannel=" + Config.CHANNEL,
                  "pluginUserId=" + PermanentInstallationID.get(),
                  "debounceValue=" + DebounceUtils.getDebounceInterval()));
      final ApplicationInfo applicationInfo = ApplicationInfo.getInstance();

      if (applicationInfo != null) {
        constantParameters.add("--client");
        constantParameters.add(cmdSanitize(applicationInfo.getVersionName()));
        constantParameters.add("--no-lsp");
        constantParameters.add("true");

        metadata.add("clientVersion=" + cmdSanitize(applicationInfo.getFullVersion()));
        metadata.add("clientApiVersion=" + cmdSanitize(applicationInfo.getApiVersion()));
      }

      if (Utils.isSelfHostedPlugin() && StaticConfig.getTabnineEnterpriseHost().isPresent()) {
        constantParameters.add(
            "--cloud2_url=" + cmdSanitize(StaticConfig.getTabnineEnterpriseHost().get()));
      }

      if (additionalMetadata != null) {
        additionalMetadata.forEach(
            (key, value) ->
                metadata.add(String.format("%s=%s", key, cmdSanitize(value.toString()))));
      }

      getLogFilePath()
          .ifPresent(
              v -> {
                constantParameters.add("--log-file-path");
                constantParameters.add(v);
              });

      getLogLevel()
          .ifPresent(
              v -> {
                constantParameters.add("--log-level");
                constantParameters.add(v);
              });

      constantParameters.addAll(metadata);
    }

    return constantParameters;
  }
}
