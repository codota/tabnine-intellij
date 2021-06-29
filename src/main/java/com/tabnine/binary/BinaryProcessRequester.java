package com.tabnine.binary;

import com.tabnine.binary.exceptions.TabNineDeadException;
import org.jetbrains.annotations.Nullable;

public interface BinaryProcessRequester {
    @Nullable
    <R extends BinaryResponse> R request(BinaryRequest<R> request) throws TabNineDeadException;

    void destroy();
}


