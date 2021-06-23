package com.tabnine.binary;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.PlatformUtils;
import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.fetch.BinaryVersionFetcher;
import com.tabnine.config.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.PermanentInstallationID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tabnine.general.StaticConfig.UNINSTALLING_FLAG;
import static com.tabnine.general.StaticConfig.getLogFilePath;
import static com.tabnine.general.Utils.cmdSanitize;
import static com.tabnine.general.Utils.getTabNinePluginVersion;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class BinaryRun {
    private final BinaryVersionFetcher binaryFetcher;

    public BinaryRun(BinaryVersionFetcher binaryFetcher) {
        this.binaryFetcher = binaryFetcher;
    }

    @NotNull
    public List<String> generateRunCommand(@Nullable Map<String, Object> additionalMetadata) throws NoValidBinaryToRunException {
        List<String> command = new ArrayList<>(singletonList(binaryFetcher.fetchBinary()));

        command.addAll(getBinaryConstantParameters(additionalMetadata));

        return command;
    }

    public Process reportUninstall(@Nullable Map<String, Object> additionalMetadata) throws NoValidBinaryToRunException, TabNineDeadException {
        String fullLocation = binaryFetcher.fetchBinary();
        List<String> command = new ArrayList<>(asList(fullLocation, UNINSTALLING_FLAG));

        command.addAll(getBinaryConstantParameters(additionalMetadata));

        try {
            return new ProcessBuilder(command).start();
        } catch (IOException e) {
            throw new TabNineDeadException(e, fullLocation);
        }
    }

    private ArrayList<String> getBinaryConstantParameters(@Nullable Map<String, Object> additionalMetadata) {
        ArrayList<String> constantParameters = new ArrayList<>();

        if (ApplicationManager.getApplication() != null && !ApplicationManager.getApplication().isUnitTestMode()) {
            List<String> metadata = new ArrayList<>(asList(
                    "--client-metadata",
                    "pluginVersion=" + cmdSanitize(getTabNinePluginVersion()),
                    "clientIsUltimate=" + PlatformUtils.isIdeaUltimate(),
                    "clientChannel=" + Config.CHANNEL,
                    "pluginUserId=" + PermanentInstallationID.get()
            ));
            final ApplicationInfo applicationInfo = ApplicationInfo.getInstance();

            if (applicationInfo != null) {
                constantParameters.add("--client");
                constantParameters.add(cmdSanitize(applicationInfo.getVersionName()));
                constantParameters.add("--no-lsp");
                constantParameters.add("true");

                metadata.add("clientVersion=" + cmdSanitize(applicationInfo.getFullVersion()));
                metadata.add("clientApiVersion=" + cmdSanitize(applicationInfo.getApiVersion()));
            }

            if (additionalMetadata != null) {
                additionalMetadata.forEach((key, value) ->
                        metadata.add(String.format("%s=%s", key, cmdSanitize(value.toString()))));
            }

            getLogFilePath().ifPresent(v -> {
                constantParameters.add("--log-file-path");
                constantParameters.add(v);
            });

            constantParameters.addAll(metadata);
        }

        return constantParameters;
    }

}
