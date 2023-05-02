package com.tabnineCommon.binary.requests.selection;

import static com.tabnineCommon.general.StaticConfig.SET_STATE_RESPONSE_RESULT_STRING;
import static java.util.Collections.singletonMap;

import com.tabnineCommon.binary.BinaryRequest;
import org.jetbrains.annotations.NotNull;

public class SetStateBinaryRequest implements BinaryRequest<SetStateBinaryResponse> {
  private final SelectionRequest selectionRequest;

  public SetStateBinaryRequest(SelectionRequest selectionRequest) {
    this.selectionRequest = selectionRequest;
  }

  @Override
  public Class<SetStateBinaryResponse> response() {
    return SetStateBinaryResponse.class;
  }

  @Override
  public Object serialize() {
    return singletonMap(
        "SetState", singletonMap("state_type", singletonMap("Selection", selectionRequest)));
  }

  @Override
  public boolean validate(@NotNull SetStateBinaryResponse response) {
    return SET_STATE_RESPONSE_RESULT_STRING.equals(response.getResult());
  }
}
