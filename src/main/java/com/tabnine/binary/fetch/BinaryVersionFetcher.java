package com.tabnine.binary.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.binary.FailedToDownloadException;
import com.tabnine.binary.NoValidBinaryToRunException;

import java.util.List;

import static com.tabnine.StaticConfig.versionFullPath;
import static java.lang.String.format;

public class BinaryVersionFetcher {
    private LocalBinaryVersions localBinaryVersions;
    private BinaryRemoteSource binaryRemoteSource;
    private BinaryDownloader binaryDownloader;

    public BinaryVersionFetcher(LocalBinaryVersions localBinaryVersions, BinaryRemoteSource binaryRemoteSource, BinaryDownloader binaryDownloader) {
        this.localBinaryVersions = localBinaryVersions;
        this.binaryRemoteSource = binaryRemoteSource;
        this.binaryDownloader = binaryDownloader;
    }

    /**
     * Fetchs TabNine's preferred version from remote server, downloads it if it is not available, and returns the path to run it.
     *
     * @return path of the binary to run
     * @throws SecurityException, NoExistingBinaryException if something went wrong
     */
    public String fetchBinary() throws NoValidBinaryToRunException {
        List<String> versions = localBinaryVersions.listExisting();

        try {
            String preferredBetaVersion = binaryRemoteSource.existingLocalBetaVersion(versions);

            if (preferredBetaVersion != null) {
                Logger.getInstance(getClass()).info(format("Binary latest beta version %s was found locally, so it being preferred.", preferredBetaVersion));
                return versionFullPath(preferredBetaVersion).toString();
            }

            String preferredVersion = binaryRemoteSource.fetchPreferredVersion();

            if (!versions.contains(preferredVersion)) {
                Logger.getInstance(getClass()).info(format("Current binary version %s not found locally, it is being downloaded.", preferredVersion));
                binaryDownloader.downloadBinary(preferredVersion);
            }

            return versionFullPath(preferredVersion).toString();
        } catch (FailedToDownloadException e) {
            Logger.getInstance(getClass()).warn("Error running current version of TabNine. Reverting to latest version available locally.", e);
        }

        return localBinaryVersions.getLatestValidVersion(versions);
    }
}
