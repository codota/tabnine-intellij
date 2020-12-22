package com.tabnine.binary.requests.config;

import com.tabnine.binary.BinaryRequest;
import com.tabnine.binary.exceptions.TabNineInvalidResponseException;
import org.jetbrains.annotations.NotNull;

import static java.util.Collections.singletonMap;

public class ConfigRequest implements BinaryRequest<ConfigResponse> {
    @Override
    public Class<ConfigResponse> response() {
        return ConfigResponse.class;
    }

    @Override
    public Object serialize() {
        return singletonMap("Configuration", new Object());
    }

    @Override
    public boolean validate(@NotNull ConfigResponse response) {
        return true;
    }

    @Override
    public boolean shouldBeAllowed(@NotNull TabNineInvalidResponseException e) {
        return true;
    }
}
