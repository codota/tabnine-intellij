package com.tabnine.binary.fetch;

import com.tabnine.binary.exceptions.FailedToDownloadException;
import java.nio.file.Path;

public interface DownloadValidator {
  void validateAndRename(Path tempDestination, Path destination) throws FailedToDownloadException;
}
