package com.tabnine.binary;

import java.io.IOException;

@FunctionalInterface
public interface SideEffectExecutor {
    void execute() throws IOException, NoValidBinaryToRunException;
}
