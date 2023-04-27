package com.tabnineCommon.binary.fetch;

import static com.tabnineCommon.general.StaticConfig.*;

import java.util.Optional;

public class BinaryDownloader {
  private final TempBinaryValidator tempBinaryValidator;
  private final GeneralDownloader downloader;

  public BinaryDownloader(TempBinaryValidator tempBinaryValidator, GeneralDownloader downloader) {
    this.tempBinaryValidator = tempBinaryValidator;
    this.downloader = downloader;
  }

  public Optional<BinaryVersion> downloadBinary(String version, String serverUrl) {
    if (serverUrl == null || serverUrl.trim().isEmpty()) {
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
