package com.tabnine.binary.requests.config;

import com.tabnine.binary.BinaryRequest;
import com.tabnine.binary.exceptions.TabNineInvalidResponseException;
import org.jetbrains.annotations.NotNull;

import static java.util.Collections.singletonMap;

public class StateRequest implements BinaryRequest<StateResponse> {
    @Override
    public Class<StateResponse> response() {
        return StateResponse.class;
    }

    @Override
    public Object serialize() {
        return singletonMap("State", new Object());
    }

    @Override
    public boolean validate(@NotNull StateResponse response) {
        return true;
    }

    @Override
    public boolean shouldBeAllowed(@NotNull TabNineInvalidResponseException e) {
        return true;
    }
}
