package com.tabnine.binary.exceptions;

public class FailedToDownloadException extends Exception {
  public FailedToDownloadException(Throwable e) {
    super(e);
  }

  public FailedToDownloadException(String s) {
    super(s);
  }
}
