package com.tabnineCommon.binary.fetch;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.text.SemVer;
import com.tabnineCommon.general.StaticConfig;
import com.tabnineCommon.lifecycle.PluginInstalled;
import com.tabnineCommon.lifecycle.PluginInstalledNotifier;
import java.util.Optional;
import java.util.prefs.Preferences;

// bootstrapper support class
// once out of beta/alpha all of this code can replace the existing fetchBinary()
public class BootstrapperSupport {
  static Optional<BinaryVersion> bootstrapVersion(
      LocalBinaryVersions localBinaryVersions,
      BinaryRemoteSource binaryRemoteSource,
      BundleDownloader bundleDownloader) {
    Optional<BinaryVersion> localBootstrapVersion =
        locateLocalBootstrapSupportedVersion(localBinaryVersions);
    if (localBootstrapVersion.isPresent()) {
      PluginInstalled.Companion.setNewInstallation(false);
      return localBootstrapVersion;
    }
    notifyPluginInstalled();
    return downloadRemoteVersion(binaryRemoteSource, bundleDownloader);
  }

  public static final String BOOTSTRAPPED_VERSION_KEY = "bootstrapped version";

  private static Preferences getPrefs() {
    return Preferences.userNodeForPackage(BootstrapperSupport.class);
  }

  private static BinaryVersion savePreferredBootstrapVersion(BinaryVersion version) {
    getPrefs().put(BOOTSTRAPPED_VERSION_KEY, version.getVersion());
    return version;
  }

  private static Optional<SemVer> minimalBootstrappedVersion() {
    return Optional.ofNullable(getPrefs().get(BOOTSTRAPPED_VERSION_KEY, null))
        .map(SemVer::parseFromText);
  }

  private static Optional<BinaryVersion> locateLocalBootstrapSupportedVersion(
      LocalBinaryVersions localBinaryVersions) {
    return minimalBootstrappedVersion()
        .flatMap(
            version -> {
              Optional<BinaryVersion> activeVersion = localBinaryVersions.activeVersion();
              if (activeVersion.isPresent()) return activeVersion;

              return localBinaryVersions.listExisting().stream()
                  .filter(v -> SemVer.parseFromText(v.getVersion()) != null)
                  .filter(v -> SemVer.parseFromText(v.getVersion()).compareTo(version) >= 0)
                  .findFirst();
            });
  }

  private static Optional<BinaryVersion> downloadRemoteVersion(
      BinaryRemoteSource binaryRemoteSource, BundleDownloader bundleDownloader) {
    Optional<String> serverUrl = StaticConfig.getTabNineBundleVersionUrl();
    return serverUrl.flatMap(
        s ->
            binaryRemoteSource
                .fetchPreferredVersion(s)
                .flatMap(bundleDownloader::downloadAndExtractBundle)
                .map(BootstrapperSupport::savePreferredBootstrapVersion));
  }

  private static void notifyPluginInstalled() {
    if (ApplicationManager.getApplication() != null) {
      PluginInstalled.Companion.setNewInstallation(true);
      // remove this once all checked and migrated to the new type
      ApplicationManager.getApplication()
          .invokeLater(
              () ->
                  ApplicationManager.getApplication()
                      .getMessageBus()
                      .syncPublisher(PluginInstalledNotifier.PLUGIN_INSTALLED_TOPIC)
                      .onPluginInstalled());
    }
  }
}
