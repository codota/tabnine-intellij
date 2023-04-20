package com.tabnineCommon.binary.fetch;

import static com.tabnineCommon.general.StaticConfig.*;

import com.tabnineCommon.general.StaticConfig;
import java.util.Optional;

public class BinaryDownloader {
  private final TempBinaryValidator tempBinaryValidator;
  private final GeneralDownloader downloader;
  private final String serverUrl;

  public BinaryDownloader(TempBinaryValidator tempBinaryValidator, GeneralDownloader downloader) {
    this(tempBinaryValidator, downloader, StaticConfig.getBundleServerUrl().orElse(null));
  }
  public BinaryDownloader(TempBinaryValidator tempBinaryValidator, GeneralDownloader downloader, String serverUrl) {
    this.tempBinaryValidator = tempBinaryValidator;
    this.downloader = downloader;
    this.serverUrl = serverUrl;
  }

  public Optional<BinaryVersion> downloadBinary(String version) {
    if (serverUrl == null) {
      return Optional.empty();
    }

    String urlString = String.join("/", serverUrl, version, TARGET_NAME, EXECUTABLE_NAME);
    String destination = versionFullPath(version);
    if (this.downloader.download(urlString, destination, tempBinaryValidator)) {
      return Optional.of(new BinaryVersion(destination, version));
    }
    return Optional.empty();
  }
}
