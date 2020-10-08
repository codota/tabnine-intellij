package com.tabnine.binary.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.general.StaticConfig;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

import static com.tabnine.general.StaticConfig.getTabNineBetaVersionUrl;
import static com.tabnine.general.Utils.readContent;

public class BinaryRemoteSource {
    @NotNull
    public Optional<String> fetchPreferredVersion() {
        try {
            return Optional.of(remoteVersionRequest(StaticConfig.getTabNineVersionUrl()));
        } catch (IOException e) {
            Logger.getInstance(getClass()).warn("Request of current version failed. Falling back to latest local version.", e);
            return Optional.empty();
        }
    }

    @Nonnull
    public Optional<BinaryVersion> existingLocalBetaVersion(List<BinaryVersion> localVersions) {
        try {
            String remoteBetaVersion = remoteVersionRequest(getTabNineBetaVersionUrl());

            return localVersions.stream().filter(version -> remoteBetaVersion.equals(version.getVersion())).findAny();
        } catch (IOException e) {
            Logger.getInstance(getClass()).warn("Request of current version failed. Falling back to latest local version.", e);
        }

        return Optional.empty();
    }

    @NotNull
    private String remoteVersionRequest(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();

        connection.setConnectTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);
        connection.setReadTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);

        return readContent(connection.getInputStream());
    }
}
