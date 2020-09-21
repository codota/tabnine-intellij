package com.tabnine;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.util.PlatformUtils;
import com.tabnine.binary.TabNineFinder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class StaticConfig {
    public static final int MAX_COMPLETIONS = 5;
    public static final String BINARY_PROTOCOL_VERSION = "2.0.2";
    public static final int COMPLETION_TIME_THRESHOLD = 1000;
    public static final int ILLEGAL_RESPONSE_THRESHOLD = 5;
    public static final int CONSECUTIVE_RESTART_THRESHOLD = 5;
    public static final int ADVERTISEMENT_MAX_LENGTH = 100;
    public static final int MAX_OFFSET = 100000; // 100 KB
    public static final String CDN_URL = "https://update.tabnine.com";
    public static final int SLEEP_TIME_BETWEEN_FAILURES = 1000;
    private static final int MAX_SLEEP_TIME_BETWEEN_FAILURES = 1000 * 60 * 60; // 1 hour

    // FIXME: This code is the highest risk code that is not tested at all.
    @NotNull
    public static List<String> generateCommand() throws IOException {
        // When we tell TabNine that it's talking to IntelliJ, it won't suggest language server
        // setup since we assume it's already built into the IDE
        List<String> command = new ArrayList<>(singletonList(TabNineFinder.getTabNinePath()));
        List<String> metadata = new ArrayList<>();
        metadata.add("--client-metadata");
        metadata.add("pluginVersion=" + Utils.getPluginVersion());
        metadata.add("clientIsUltimate=" + PlatformUtils.isIdeaUltimate());
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

        return command;
    }

    public static void sleepUponFailure(int attempt) throws InterruptedException {
        Thread.sleep(Math.min(exponentialBackoff(attempt), MAX_SLEEP_TIME_BETWEEN_FAILURES));
    }

    private static int exponentialBackoff(int attempt) {
        return SLEEP_TIME_BETWEEN_FAILURES * (int) Math.pow(2, Math.min(attempt, 30));
    }

    /**
     * We would never like the plugin to stop trying to reload the binary. For it to not bombard the user, there is an
     * executeSleepStrategy.
     *
     * @param attempt
     * @return
     */
    public static boolean shouldTryStartingBinary(int attempt) {
        return true;
    }
}
