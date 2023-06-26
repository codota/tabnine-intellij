package com.tabnineCommon.binary;

import com.tabnineCommon.binary.exceptions.TabNineDeadException;
import org.jetbrains.annotations.Nullable;

public interface BinaryProcessRequester {
  @Nullable
  <R> R request(BinaryRequest<R> request) throws TabNineDeadException;

  Long pid();

  void destroy();
}
