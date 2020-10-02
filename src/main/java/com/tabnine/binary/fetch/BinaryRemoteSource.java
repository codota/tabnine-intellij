package com.tabnine.binary.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.StaticConfig;
import com.tabnine.Utils;
import com.tabnine.binary.FailedToDownloadException;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static com.tabnine.Utils.readContent;

public class BinaryRemoteSource {
    @NotNull
    public String fetchPreferredVersion() throws FailedToDownloadException {
        try {
            return remoteVersionRequest(StaticConfig.getTabNineVersionUrl());
        } catch (IOException e) {
            Logger.getInstance(getClass()).warn("Request of current version failed. Falling back to latest local version.", e);
            throw new FailedToDownloadException(e);
        }
    }

    @Nullable
    public String existingLocalBetaVersion(List<String> localVersions) {
        try {
            String remoteBetaVersion = remoteVersionRequest(StaticConfig.getTabNineBetaVersionUrl());

            if(localVersions.contains(remoteBetaVersion)) {
                return remoteBetaVersion;
            }
        } catch (IOException e) {
            Logger.getInstance(getClass()).warn("Request of current version failed. Falling back to latest local version.", e);
        }

        return null;
    }

    @NotNull
    private String remoteVersionRequest(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();

        connection.setConnectTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);
        connection.setReadTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);

        return readContent(connection.getInputStream());
    }
}
