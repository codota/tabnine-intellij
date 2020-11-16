package com.tabnine.binary.requests.selection;

import com.tabnine.binary.BinaryRequest;
import org.jetbrains.annotations.NotNull;

import static com.tabnine.general.StaticConfig.SET_STATE_RESPONSE_RESULT_STRING;
import static java.util.Collections.singletonMap;

public class SetStateBinaryRequest implements BinaryRequest<SetStateBinaryResponse> {
    private SelectionRequest selectionRequest;

    public SetStateBinaryRequest(SelectionRequest selectionRequest) {
        this.selectionRequest = selectionRequest;
    }

    @Override
    public Class<SetStateBinaryResponse> response() {
        return SetStateBinaryResponse.class;
    }

    @Override
    public Object serialize() {
        return singletonMap("SetState", singletonMap("state_type", singletonMap("Selection", selectionRequest)));
    }

    @Override
    public boolean validate(@NotNull SetStateBinaryResponse response) {
        return SET_STATE_RESPONSE_RESULT_STRING.equals(response.getResult());
    }
}
