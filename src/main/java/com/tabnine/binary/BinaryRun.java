package com.tabnine.binary;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.PlatformUtils;
import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.fetch.BinaryVersionFetcher;
import com.tabnine.config.Config;
import com.tabnine.general.StaticConfig;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.tabnine.general.StaticConfig.*;
import static com.tabnine.general.StaticConfig.UNINSTALLING_FLAG;
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
    public List<String> getBinaryRunCommand() throws NoValidBinaryToRunException {
        List<String> command = new ArrayList<>(singletonList(binaryFetcher.fetchBinary()));

        command.addAll(getBinaryConstantParameters());

        return command;
    }

    public Process reportUninstall(String... additionalMetadata) throws NoValidBinaryToRunException, TabNineDeadException {
        String fullLocation = binaryFetcher.fetchBinary();
        List<String> command = new ArrayList<>(asList(fullLocation, UNINSTALLING_FLAG));

        command.addAll(getBinaryConstantParameters());
        command.addAll(asList(additionalMetadata));

        try {
            return new ProcessBuilder(command).start();
        } catch (IOException e) {
            throw new TabNineDeadException(e, fullLocation);
        }
    }

    private ArrayList<String> getBinaryConstantParameters() {
        ArrayList<String> constantParameters = new ArrayList<>();

        if(ApplicationManager.getApplication() != null && !ApplicationManager.getApplication().isUnitTestMode()) {
            List<String> metadata = new ArrayList<>(asList(
                    "--client-metadata",
                    "pluginVersion=" + cmdSanitize(getTabNinePluginVersion()),
                    "clientIsUltimate=" + PlatformUtils.isIdeaUltimate(),
                    "clientChannel=" + Config.CHANNEL
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

            getLogFilePath().ifPresent(v -> {
                constantParameters.add("--log-file-path");
                constantParameters.add(v);
            });

            constantParameters.addAll(metadata);
        }

        return constantParameters;
    }

}
