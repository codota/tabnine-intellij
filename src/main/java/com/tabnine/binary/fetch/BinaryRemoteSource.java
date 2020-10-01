package com.tabnine.binary.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.StaticConfig;
import com.tabnine.Utils;
import com.tabnine.binary.FailedToDownloadException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class BinaryRemoteSource {
    @NotNull
    public String fetchPreferredVersion() throws FailedToDownloadException {
        try {
            URLConnection connection = new URL(StaticConfig.getTabNineVersionUrl()).openConnection();

            connection.setConnectTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);
            connection.setReadTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);

            return Utils.readContent(connection.getInputStream());
        } catch (IOException e) {
            Logger.getInstance(getClass()).warn("Request of current version failed. Falling back to latest local version.", e);
            throw new FailedToDownloadException(e);
        }
    }
}
