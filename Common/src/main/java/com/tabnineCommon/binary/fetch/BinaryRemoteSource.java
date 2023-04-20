package com.tabnineCommon.binary.fetch;

import static com.tabnineCommon.general.StaticConfig.getTabNineBetaVersionUrl;
import static com.tabnineCommon.general.Utils.readContent;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnineCommon.general.StaticConfig;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class BinaryRemoteSource {
  @NotNull
  public Optional<String> fetchPreferredVersion() {
    Optional<String> serverUrl = StaticConfig.getTabNineBundleVersionUrl();
    if (serverUrl.isPresent()) {
      return fetchPreferredVersion(serverUrl.get());
    }
    return Optional.empty();
  }

  public Optional<String> fetchPreferredVersion(String url) {
    try {
      return Optional.of(remoteVersionRequest(url));
    } catch (IOException e) {
      Logger.getInstance(getClass())
          .warn("Request of current version failed. Falling back to latest local version.", e);
      return Optional.empty();
    }
  }

  @NotNull
  public Optional<BinaryVersion> existingLocalBetaVersion(List<BinaryVersion> localVersions) {
    try {
      String remoteBetaVersion = remoteVersionRequest(getTabNineBetaVersionUrl());

      return localVersions.stream()
          .filter(version -> remoteBetaVersion.equals(version.getVersion()))
          .findAny();
    } catch (IOException e) {
      Logger.getInstance(getClass())
          .warn("Request of current version failed. Falling back to latest local version.", e);
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
