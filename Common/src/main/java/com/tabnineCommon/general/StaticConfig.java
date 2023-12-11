package com.tabnineCommon.general;

import static com.tabnineCommon.general.Utils.isUnitTestMode;
import static java.awt.Color.decode;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.ObjectUtils;
import com.intellij.util.text.SemVer;
import com.tabnineCommon.binary.exceptions.InvalidVersionPathException;
import com.tabnineCommon.config.Config;
import com.tabnineCommon.userSettings.AppSettingsState;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public class StaticConfig {
  // Must be identical to what is written under <id>com.tabnine.TabNine</id> in plugin.xml !!!
  public static final String TABNINE_PLUGIN_ID_RAW = "com.tabnine.TabNine";
  public static final PluginId TABNINE_PLUGIN_ID = PluginId.getId(TABNINE_PLUGIN_ID_RAW);
  public static final int MAX_COMPLETIONS = 5;
  public static final String BINARY_PROTOCOL_VERSION = "4.4.223";
  public static final int COMPLETION_TIME_THRESHOLD = 1000;
  public static final int NEWLINE_COMPLETION_TIME_THRESHOLD = 3000;
  public static final int ILLEGAL_RESPONSE_THRESHOLD = 5;
  public static final int ADVERTISEMENT_MAX_LENGTH = 100;
  public static final int MAX_OFFSET = 100000; // 100 KB
  public static final int SLEEP_TIME_BETWEEN_FAILURES = 1000;
  public static final int BINARY_MINIMUM_REASONABLE_SIZE = 1000 * 1000; // roughly 1MB
  public static final String SET_STATE_RESPONSE_RESULT_STRING = "Done";
  public static final String UNINSTALLING_FLAG = "--uninstalling";
  public static final int BINARY_TIMEOUTS_THRESHOLD_MILLIS = 60_000;
  public static final String BRAND_NAME = "tabnine";
  public static final String TARGET_NAME = getDistributionName();
  public static final String EXECUTABLE_NAME = getExeName();
  public static final String TABNINE_FOLDER_NAME =
      Config.IS_SELF_HOSTED ? ".tabnine-enterprise" : ".tabnine";
  public static final int BINARY_READ_TIMEOUT = 5 * 60 * 1000; // 5 minutes
  public static final int REMOTE_CONNECTION_TIMEOUT = 5_000; // 5 seconds
  public static final long BINARY_NOTIFICATION_POLLING_INTERVAL = 10_000L; // 10 seconds
  public static final String USER_HOME_PATH_PROPERTY = "user.home";
  public static final String REMOTE_BASE_URL_PROPERTY = "TABNINE_REMOTE_BASE_URL";
  public static final String REMOTE_VERSION_URL_PROPERTY = "TABNINE_REMOTE_VERSION_URL";
  public static final String TABNINE_ENTERPRISE_HOST = "TABNINE_ENTERPRISE_HOST";
  public static final String REMOTE_BETA_VERSION_URL_PROPERTY = "TABNINE_REMOTE_BETA_VERSION_URL";
  public static final String LOG_FILE_PATH_PROPERTY = "TABNINE_LOG_FILE_PATH";
  public static final String ICON_AND_NAME_PATH = "icons/tabnine-starter-13px.png";
  public static final String LIMITATION_SYMBOL = "🔒";
  public static final Color PROMOTION_TEXT_COLOR = decode("#e12fee");
  public static final Color PROMOTION_LIGHT_TEXT_COLOR = decode("#FF99FF");
  private static final int MAX_SLEEP_TIME_BETWEEN_FAILURES = 1_000 * 60 * 60; // 1 hour
  public static final long BINARY_PROMOTION_POLLING_INTERVAL = 2 * 60 * 1_000L; // 2 minutes
  public static final long BINARY_PROMOTION_POLLING_DELAY = 10_000L; // 10 seconds

  public static final String OPEN_HUB_ACTION = "OpenHub";

  public static Icon getTabnineIcon() {
    return IconLoader.findIcon("/icons/tabnine-icon-13px.png");
  }

  public static Icon getTabnineLensIcon() {
    return IconLoader.findIcon("/icons/tabnine-lens-icon.png");
  }

  public static Icon getGlyphIcon() {
    return IconLoader.findIcon("icons/icon-13px.png");
  }

  public static Icon getProblemGlyphIcon() {
    return IconLoader.findIcon("icons/problem-icon-13px.png");
  }

  public static Icon getIconAndName() {
    return IconLoader.findIcon("/icons/tabnine-13px.png");
  }

  public static Icon getIconAndNameStarter() {
    return IconLoader.findIcon("/icons/tabnine-starter-13px.png");
  }

  public static Icon getIconAndNamePro() {
    return IconLoader.findIcon("/icons/tabnine-pro-13px.png");
  }

  public static Icon getIconAndNameEnterprise() {
    return IconLoader.findIcon("/icons/tabnine-enterprise-13px.png");
  }

  public static Icon getNotificationIcon() {
    return IconLoader.findIcon("/icons/tabnine-icon-13px.png");
  }

  public static Icon getConnectionLostNotificationIcon() {
    return IconLoader.findIcon("/icons/tabnine-connection-lost-notification-icon.png");
  }

  public static Icon getIconAndNameConnectionLostStarter() {
    return IconLoader.findIcon("/icons/tabnine-starter-connection-lost-13px.png");
  }

  public static Icon getIconAndNameConnectionLostPro() {
    return IconLoader.findIcon("/icons/tabnine-pro-connection-lost-13px.png");
  }

  public static Icon getIconAndNameConnectionLostEnterprise() {
    return IconLoader.findIcon("/icons/tabnine-enterprise-connection-lost-13px.png");
  }

  // As far as I understand, This registers `BRAND_NAME` display id as a "sticky balloon" type,
  // so that when we create a notification with this display id it knows to make it a sticky
  // balloon.
  public static NotificationGroup TABNINE_NOTIFICATION_GROUP =
      new NotificationGroup(StaticConfig.BRAND_NAME, NotificationDisplayType.STICKY_BALLOON, false);

  public static NotificationGroup TABNINE_TIMED_NOTIFICATION_GROUP =
      new NotificationGroup(StaticConfig.BRAND_NAME, NotificationDisplayType.BALLOON, false);

  public static Optional<String> getLogFilePath() {
    String logFilePathFromUserSettings = AppSettingsState.getInstance().getLogFilePath();
    if (!logFilePathFromUserSettings.isEmpty()) {
      return Optional.of(logFilePathFromUserSettings);
    }

    return Optional.ofNullable(System.getProperty(LOG_FILE_PATH_PROPERTY));
  }

  public static Optional<String> getLogLevel() {
    String logLevelFromUserSettings = AppSettingsState.getInstance().getLogLevel();
    if (!logLevelFromUserSettings.isEmpty()) {
      return Optional.of(logLevelFromUserSettings);
    }

    return Optional.empty();
  }

  public static Boolean getIgnoreCertificateErrors() {
    return AppSettingsState.getInstance().getIgnoreCertificateErrors();
  }

  public static Optional<String> getTabnineEnterpriseHost() {
    String path = AppSettingsState.getInstance().getCloud2Url();
    if (!path.isEmpty()) {
      return Optional.of(path);
    }
    return Optional.ofNullable(System.getProperty(TABNINE_ENTERPRISE_HOST));
  }

  public static String getServerUrl() {
    return Optional.ofNullable(System.getProperty(REMOTE_BASE_URL_PROPERTY))
        .orElse("https://update.tabnine.com");
  }

  public static Optional<String> getBundleServerUrl() {
    if (Config.IS_SELF_HOSTED) {
      Optional<String> tabnineEnterpriseHost = StaticConfig.getTabnineEnterpriseHost();
      if (!tabnineEnterpriseHost.isPresent()) {
        Logger.getInstance(StaticConfig.class).warn("On prem version but server url not set");
        return Optional.empty();
      }
      return Optional.of(String.join("/", tabnineEnterpriseHost.get(), "update", "bundles"));
    }

    return Optional.of(
        Optional.ofNullable(System.getProperty(REMOTE_BASE_URL_PROPERTY))
            .orElse("https://update.tabnine.com/bundles"));
  }

  @NotNull
  public static Optional<String> getTabNineBundleVersionUrl() {
    return Optional.ofNullable(System.getProperty(REMOTE_VERSION_URL_PROPERTY))
        .or(() -> StaticConfig.getBundleServerUrl().map(s -> s + "/version"));
  }

  @NotNull
  public static String getTabNineBetaVersionUrl() {
    return Optional.ofNullable(System.getProperty(REMOTE_BETA_VERSION_URL_PROPERTY))
        .orElse(getServerUrl() + "/beta_version");
  }

  public static void sleepUponFailure(int attempt) throws InterruptedException {
    Thread.sleep(Math.min(exponentialBackoff(attempt), MAX_SLEEP_TIME_BETWEEN_FAILURES));
  }

  public static int exponentialBackoff(int attempt) {
    if (isUnitTestMode()) {
      return 0;
    }
    return SLEEP_TIME_BETWEEN_FAILURES * (int) Math.pow(2, Math.min(attempt, 30));
  }

  private static String getDistributionName() {
    String arch = SystemInfo.is32Bit ? "i686" : "x86_64";
    if ("aarch64".equals(System.getProperty("os.arch"))) {
      arch = "aarch64";
    }

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
      throw new RuntimeException(
          "Platform was not recognized as any of Windows, macOS, Linux, FreeBSD");
    }

    return arch + "-" + platform;
  }

  public static Path getBaseDirectory() {
    // Unit tests don't initialize the application, so `ApplicationManager.getApplication` will
    // return null.
    Boolean isUnitTest =
        ObjectUtils.doIfNotNull(ApplicationManager.getApplication(), Application::isUnitTestMode);
    if (isUnitTest == null || isUnitTest) {
      return getDefaultBaseDirectory();
    }

    String binariesFolderOverride = AppSettingsState.getInstance().getBinariesFolderOverride();

    if (!binariesFolderOverride.isEmpty()) {
      return Paths.get(binariesFolderOverride);
    }

    return getDefaultBaseDirectory();
  }

  public static Path getDefaultBaseDirectory() {
    return Paths.get(System.getProperty(USER_HOME_PATH_PROPERTY), TABNINE_FOLDER_NAME);
  }

  public static Path getActiveVersionPath() {
    return getBaseDirectory().resolve(".active");
  }

  private static String getExeName() {
    return SystemInfo.isWindows ? "TabNine.exe" : "TabNine";
  }

  @NotNull
  public static String versionFullPath(String version) throws InvalidVersionPathException {
    return Paths.get(
            getBaseDirectory().toString(), validVersion(version), TARGET_NAME, EXECUTABLE_NAME)
        .toString();
  }

  @NotNull
  public static String bundleFullPath(String version) {
    return Paths.get(
            getBaseDirectory().toString(), validVersion(version), TARGET_NAME, "TabNine.zip")
        .toString();
  }

  @NotNull
  public static Map<String, Object> wrapWithBinaryRequest(Object value) {
    Map<String, Object> jsonObject = new HashMap<>();

    jsonObject.put("version", BINARY_PROTOCOL_VERSION);
    jsonObject.put("request", value);

    return jsonObject;
  }

  @NotNull
  private static String validVersion(String version) throws InvalidVersionPathException {
    SemVer semVer = SemVer.parseFromText(version);
    if (semVer == null) {
      throw new InvalidVersionPathException(version);
    }

    return semVer.toString();
  }
}
