package com.tabnine.lifecycle;

import static java.lang.String.format;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnineCommon.binary.BinaryRun;
import com.tabnineCommon.binary.exceptions.NoValidBinaryToRunException;
import com.tabnineCommon.binary.exceptions.TabNineDeadException;
import java.util.Map;

public class UninstallReporter {
  private final BinaryRun binaryRun;

  public UninstallReporter(BinaryRun binaryRun) {
    this.binaryRun = binaryRun;
  }

  public void reportUninstall(Map<String, Object> additionalMetadata) {
    try {
      binaryRun.reportUninstall(additionalMetadata).waitFor();
    } catch (NoValidBinaryToRunException e) {
      Logger.getInstance(getClass()).warn("Couldn't find a binary to run", e);
    } catch (TabNineDeadException e) {
      Logger.getInstance(getClass())
          .warn(format("Couldn't communicate uninstalling to binary at: %s", e.getLocation()), e);
    } catch (InterruptedException e) {
      Logger.getInstance(getClass()).warn("Couldn't communicate uninstalling to binary.", e);
    }
  }
}
