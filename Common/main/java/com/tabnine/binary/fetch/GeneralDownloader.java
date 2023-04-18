package com.tabnine.binary.fetch;

import static com.tabnine.general.StaticConfig.BINARY_READ_TIMEOUT;
import static com.tabnine.general.StaticConfig.REMOTE_CONNECTION_TIMEOUT;
import static java.lang.String.format;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.binary.exceptions.FailedToDownloadException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class GeneralDownloader {
  boolean download(String urlString, String destination, DownloadValidator validator) {
    Path tempDestination = Paths.get(format("%s.download.%s", destination, UUID.randomUUID()));

    try {
      if (!tempDestination.getParent().toFile().exists()) {
        if (!tempDestination.getParent().toFile().mkdirs()) {
          Logger.getInstance(GeneralDownloader.class)
              .warn(format("Could not create the required directories for %s", tempDestination));
          return false;
        }
      }
      URLConnection connection = new URL(urlString).openConnection();
      connection.setConnectTimeout(REMOTE_CONNECTION_TIMEOUT);
      connection.setReadTimeout(BINARY_READ_TIMEOUT);
      Files.copy(connection.getInputStream(), tempDestination, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      Logger.getInstance(GeneralDownloader.class).warn(e);
      return false;
    }
    try {
      validator.validateAndRename(tempDestination, Paths.get(destination));
    } catch (FailedToDownloadException e) {
      Logger.getInstance(GeneralDownloader.class).warn(e);
      tempDestination.toFile().delete();
      return false;
    }

    return true;
  }
}
