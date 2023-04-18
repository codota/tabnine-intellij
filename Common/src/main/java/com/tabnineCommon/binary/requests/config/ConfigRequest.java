package com.tabnineCommon.binary.requests.config;

import static java.util.Collections.singletonMap;

import com.tabnineCommon.binary.BinaryRequest;
import com.tabnineCommon.binary.exceptions.TabNineInvalidResponseException;
import org.jetbrains.annotations.NotNull;

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
