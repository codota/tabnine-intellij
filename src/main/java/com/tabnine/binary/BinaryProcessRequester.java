package com.tabnine.binary;

import com.tabnine.binary.exceptions.TabNineDeadException;

import javax.annotation.Nullable;

public interface BinaryProcessRequester {
    @Nullable
    <R extends BinaryResponse> R request(BinaryRequest<R> request) throws TabNineDeadException;
}
