package com.tabnine.binary.fetch;

import static com.tabnine.general.StaticConfig.BINARY_MINIMUM_REASONABLE_SIZE;
import static java.lang.String.format;

import com.tabnine.binary.exceptions.FailedToDownloadException;
import java.nio.file.Path;

public class TempBinaryValidator implements DownloadValidator {
  private final BinaryValidator binaryValidator;

  public TempBinaryValidator(BinaryValidator binaryValidator) {
    this.binaryValidator = binaryValidator;
  }

  public void validateAndRename(Path tempDestination, Path destination)
      throws FailedToDownloadException {
    if (!tempDestination.toFile().exists()) {
      throw new FailedToDownloadException(
          "Temp file was not found at " + tempDestination.toString());
    }

    if (tempDestination.toFile().length() < BINARY_MINIMUM_REASONABLE_SIZE) {
      throw new FailedToDownloadException("Binary content from server was corrupt");
    }

    if (!tempDestination.toFile().setExecutable(true)) {
      throw new FailedToDownloadException("Couldn't set execute permission on " + tempDestination);
    }

    if (!binaryValidator.isWorking(tempDestination.toString())) {
      throw new FailedToDownloadException(format("Could run to validate at: %s", tempDestination));
    }

    if (!tempDestination.toFile().renameTo(destination.toFile())
        || !destination.toFile().exists()) {
      throw new FailedToDownloadException(
          "Although downloaded successfully and without errors, Tabnine's binary does not exists in the detination folder: "
              + destination.toString());
    }
  }
}
