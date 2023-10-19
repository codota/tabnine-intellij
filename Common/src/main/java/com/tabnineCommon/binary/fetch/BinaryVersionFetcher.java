package com.tabnineCommon.binary.fetch;

import static java.lang.String.format;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.text.SemVer;
import com.tabnineCommon.binary.exceptions.NoValidBinaryToRunException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class BinaryVersionFetcher {
  private LocalBinaryVersions localBinaryVersions;
  private BinaryRemoteSource binaryRemoteSource;
  private BinaryDownloader binaryDownloader;
  private BundleDownloader bundleDownloader;

  public BinaryVersionFetcher(
      LocalBinaryVersions localBinaryVersions,
      BinaryRemoteSource binaryRemoteSource,
      BinaryDownloader binaryDownloader,
      BundleDownloader bundleDownloader) {
    this.localBinaryVersions = localBinaryVersions;
    this.binaryRemoteSource = binaryRemoteSource;
    this.binaryDownloader = binaryDownloader;
    this.bundleDownloader = bundleDownloader;
  }

  /**
   * Fetchs TabNine's preferred version from remote server, downloads it if it is not available, and
   * returns the path to run it.
   *
   * @return path of the binary to run
   * @throws SecurityException, NoExistingBinaryException if something went wrong
   */
  public BinaryVersion fetchBinary() throws NoValidBinaryToRunException {
    Optional<BinaryVersion> bootstrappedVersion =
        BootstrapperSupport.bootstrapVersion(
            localBinaryVersions, binaryRemoteSource, bundleDownloader);
    if (bootstrappedVersion.isPresent()) {
      Logger.getInstance(getClass())
          .info(format("found local bootstrapped version %s", bootstrappedVersion.get()));
      return bootstrappedVersion.get();
    }
    Logger.getInstance(getClass())
        .warn("couldn't get bootstrapped version. fallback to legacy code!");
    List<BinaryVersion> versions = localBinaryVersions.listExisting();

    Optional<BinaryVersion> preferredBetaVersion =
        binaryRemoteSource.existingLocalBetaVersion(versions);

    if (preferredBetaVersion.isPresent()) {
      Logger.getInstance(getClass())
          .info(
              format(
                  "Binary latest beta version %s was found locally, so it is being preferred.",
                  preferredBetaVersion.get().getVersion()));

      if (!isBadVersion(preferredBetaVersion.get())) {
        return preferredBetaVersion.get();
      }
    }

    return binaryRemoteSource
        .fetchPreferredVersion()
        .map(getLocalOrDownload(versions))
        .orElseGet(versions.stream()::findFirst)
        .orElseThrow(NoValidBinaryToRunException::new);
  }

  private Function<String, Optional<BinaryVersion>> getLocalOrDownload(
      List<BinaryVersion> versions) {
    return preferred -> {
      Optional<BinaryVersion> localVersion =
          versions.stream().filter(version -> preferred.equals(version.getVersion())).findAny();

      if (localVersion.isPresent()) {
        return localVersion;
      }

      Logger.getInstance(getClass())
          .info(
              format(
                  "Current binary version %s not found locally, it is being downloaded.",
                  preferred));

      return binaryDownloader.downloadBinary(preferred);
    };
  }

  static boolean isBadVersion(BinaryVersion binaryVersion) {
    SemVer semver = SemVer.parseFromText(binaryVersion.getVersion());

    if (semver == null) {
      return false;
    }

    return semver.isGreaterOrEqualThan(4, 5, 0) && !semver.isGreaterOrEqualThan(4, 5, 13);
  }
}
