package com.tabnine.state;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.binary.BinaryRun;
import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import com.tabnine.binary.exceptions.TabNineDeadException;

import static java.lang.String.format;

public class UninstallReporter {
    private final BinaryRun binaryRun;

    public UninstallReporter(BinaryRun binaryRun) {
        this.binaryRun = binaryRun;
    }

    public void reportUninstall(String... additionalMetadata) {
        try {
            binaryRun.reportUninstall(additionalMetadata).waitFor();
        } catch (NoValidBinaryToRunException e) {
            Logger.getInstance(getClass()).warn("Couldn't find a binary to run", e);
        } catch (TabNineDeadException e) {
            Logger.getInstance(getClass()).warn(format("Couldn't communicate uninstalling to binary at: %s", e.getLocation()), e);
        } catch (InterruptedException e) {
            Logger.getInstance(getClass()).warn("Couldn't communicate uninstalling to binary.", e);
        }
    }
}
