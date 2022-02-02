package com.tabnine.binary;

import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import java.io.IOException;

@FunctionalInterface
public interface SideEffectExecutor {
  void execute() throws IOException, NoValidBinaryToRunException;
}
