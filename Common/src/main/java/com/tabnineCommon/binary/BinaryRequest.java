package com.tabnineCommon.binary;

import com.tabnineCommon.binary.exceptions.TabNineInvalidResponseException;
import org.jetbrains.annotations.NotNull;

public interface BinaryRequest<R extends BinaryResponse> {
  Class<R> response();

  Object serialize();

  default boolean validate(@NotNull R response) {
    return true;
  }

  default boolean shouldBeAllowed(@NotNull TabNineInvalidResponseException e) {
    return false;
  }
}
