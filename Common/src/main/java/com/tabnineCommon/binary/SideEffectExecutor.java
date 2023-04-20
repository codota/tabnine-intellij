package com.tabnineCommon.binary;

import com.tabnineCommon.binary.exceptions.NoValidBinaryToRunException;
import java.io.IOException;

@FunctionalInterface
public interface SideEffectExecutor {
  void execute() throws IOException, NoValidBinaryToRunException;
}
