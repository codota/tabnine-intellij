package com.tabnine;

import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class StaticConfig {
    public static final int MAX_COMPLETIONS = 5;
    public static final String BINARY_PROTOCOL_VERSION = "2.0.2";
    public static final int COMPLETION_TIME_THRESHOLD = 1000;
    public static final int ILLEGAL_RESPONSE_THRESHOLD = 5;
    public static final int CONSECUTIVE_RESTART_THRESHOLD = 5;
    public static final int ADVERTISEMENT_MAX_LENGTH = 100;
    public static final int MAX_OFFSET = 100000; // 100 KB
    public static final int SLEEP_TIME_BETWEEN_FAILURES = 1000;
    public static final int BINARY_MINIMUM_REASONABLE_SIZE = 1000 * 1000; // roughly 1MB
    private static final int MAX_SLEEP_TIME_BETWEEN_FAILURES = 1000 * 60 * 60; // 1 hour
    public static final String TARGET_NAME = getDistributionName();
    public static final String EXECUTABLE_NAME = getExeName();
    public static final String TABNINE_FOLDER_NAME = ".tabnine";

    public static final int BINARY_READ_TIMEOUT = 5 * 60 * 1000; // 5 minutes
    public static final int REMOTE_CONNECTION_TIMEOUT = 5 * 1000; // 5 seconds

    public static final String USER_HOME_PATH_PROPERTY = "user.home";
    public static final String REMOTE_BASE_URL_PROPERTY = "TABNINE_REMOTE_BASE_URL";
    public static final String REMOTE_VERSION_URL_PROPERTY = "TABNINE_REMOTE_VERSION_URL";
    public static final String REMOTE_BETA_VERSION_URL_PROPERTY = "TABNINE_REMOTE_BETA_VERSION_URL";

    public static String getServerUrl() {
        return Optional.ofNullable(System.getProperty(REMOTE_BASE_URL_PROPERTY)).orElse("https://update.tabnine.com");
    }

    @NotNull
    public static String getTabNineVersionUrl() {
        return Optional.ofNullable(System.getProperty(REMOTE_VERSION_URL_PROPERTY)).orElse(getServerUrl() + "/version");
    }

    @NotNull
    public static String getTabNineBetaVersionUrl() {
        return Optional.ofNullable(System.getProperty(REMOTE_BETA_VERSION_URL_PROPERTY)).orElse(getServerUrl() + "/beta_version");
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

    private static String getDistributionName() {
        String is32or64 = SystemInfo.is32Bit ? "i686" : "x86_64";
        String platform;

        if (SystemInfo.isWindows) {
            platform = "pc-windows-gnu";
        } else if (SystemInfo.isMac) {
            platform = "apple-darwin";
        } else if (SystemInfo.isLinux) {
            platform = "unknown-linux-musl";
        } else if (SystemInfo.isFreeBSD) {
            platform = "unknown-freebsd";
        } else {
            throw new RuntimeException("Platform was not recognized as any of Windows, macOS, Linux, FreeBSD");
        }

        return is32or64 + "-" + platform;
    }

    public static Path getBaseDirectory() {
        return Paths.get(System.getProperty(USER_HOME_PATH_PROPERTY), TABNINE_FOLDER_NAME);
    }

    private static String getExeName() {
        return SystemInfo.isWindows ? "TabNine.exe" : "TabNine";
    }

    @NotNull
    public static Path versionFullPath(String version) {
        return Paths.get(getBaseDirectory().toString(), version, TARGET_NAME, EXECUTABLE_NAME);
    }
}
