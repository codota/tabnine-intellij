package com.tabnine.selections;

import com.tabnine.binary.exceptions.TabNineInvalidResponseException;
import org.jetbrains.annotations.NotNull;

public interface BinaryRequest<R extends BinaryResponse> {
    Class<R> response();
    Object serialize(int correlationId);
    boolean validate(@NotNull R response);
    
    default boolean shouldBeAllowed(TabNineInvalidResponseException e) {
        return false;
    }
}