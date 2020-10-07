package com.tabnine.binary;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.PlatformUtils;
import com.tabnine.Utils;
import com.tabnine.binary.fetch.BinaryVersionFetcher;
import com.tabnine.exceptions.TabNineDeadException;
import com.tabnine.config.Config;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tabnine.StaticConfig.UNINSTALLING_FLAG;
import static com.tabnine.Utils.cmdSanitize;
import static com.tabnine.Utils.getTabNinePluginVersion;
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

    public Process reportUninstall() throws NoValidBinaryToRunException, TabNineDeadException {
        String fullLocation = binaryFetcher.fetchBinary();
        List<String> command = new ArrayList<>(asList(fullLocation, UNINSTALLING_FLAG));

        command.addAll(getBinaryConstantParameters());

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
            constantParameters.addAll(metadata);
        }

        return constantParameters;
    }

}
