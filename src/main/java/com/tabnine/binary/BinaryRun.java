package com.tabnine.binary;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.PlatformUtils;
import com.tabnine.Utils;
import com.tabnine.binary.fetch.BinaryVersionFetcher;
import com.tabnine.config.Config;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

        if(ApplicationManager.getApplication() != null && !ApplicationManager.getApplication().isUnitTestMode()) {
            List<String> metadata = new ArrayList<>(asList(
                    "--client-metadata",
                    "pluginVersion=" + Utils.getPluginVersion(),
                    "clientIsUltimate=" + PlatformUtils.isIdeaUltimate(),
                    "clientChannel=" + Config.CHANNEL
            ));
            final ApplicationInfo applicationInfo = ApplicationInfo.getInstance();

            if (applicationInfo != null) {
                command.add("--client");
                command.add(applicationInfo.getVersionName());
                command.add("--no-lsp");
                command.add("true");
                metadata.add("clientVersion=" + applicationInfo.getFullVersion());
                metadata.add("clientApiVersion=" + applicationInfo.getApiVersion());
            }
            command.addAll(metadata);
        }

        return command;
    }

}
