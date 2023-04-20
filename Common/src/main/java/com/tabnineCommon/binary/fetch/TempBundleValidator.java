package com.tabnineCommon.binary.fetch;

import com.tabnineCommon.binary.exceptions.FailedToDownloadException;
import java.nio.file.Path;

public class TempBundleValidator implements DownloadValidator {
  @Override
  public void validateAndRename(Path tempDestination, Path destination)
      throws FailedToDownloadException {
    if (!tempDestination.toFile().exists()) {
      throw new FailedToDownloadException(
          "Temp file was not found at " + tempDestination.toString());
    }

    if (!tempDestination.toFile().renameTo(destination.toFile())
        || !destination.toFile().exists()) {
      throw new FailedToDownloadException(
          "Although downloaded successfully and without errors, TabNine's binary does not exists in the detination folder: "
              + destination.toString());
    }
  }
}
