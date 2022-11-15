package com.tabnine.binary.fetch;

import com.intellij.util.text.SemVer;
import com.tabnine.general.GettingStartedManager;
import com.tabnine.general.StaticConfig;
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
      return localBootstrapVersion;
    }
    GettingStartedManager.handleFirstTimePreview();
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
    return binaryRemoteSource
        .fetchPreferredVersion(StaticConfig.getTabNineBundleVersionUrl())
        .flatMap(bundleDownloader::downloadAndExtractBundle)
        .map(BootstrapperSupport::savePreferredBootstrapVersion);
  }
}
