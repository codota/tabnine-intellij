package com.tabnine.selections;

import org.jetbrains.annotations.NotNull;

public interface BinaryRequest<T, R extends BinaryResponse> {
    Class<R> response();
    T serialize(int correlationId);
    boolean validate(@NotNull R response);
}