package com.tabnine;

import com.intellij.openapi.util.SystemInfo;

import java.nio.file.Path;
import java.nio.file.Paths;

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
    public static final long WAIT_FOR_BINARY_TO_FINISH_LOADING = 10 * 1000; // 20 seconds
    private static final int MAX_SLEEP_TIME_BETWEEN_FAILURES = 1000 * 60 * 60; // 1 hour
    public static final String TARGET_NAME = getDistributionName();
    public static final Path BINARY_DIRECTORY = getTabNineDirectory();
    public static final String EXECUTABLE_NAME = getExeName();

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

    private static Path getTabNineDirectory() {
        return Paths.get(System.getProperty("user.home"), ".tabnine");
    }

    private static String getExeName() {
        return SystemInfo.isWindows ? "TabNine.exe" : "TabNine";
    }
}
