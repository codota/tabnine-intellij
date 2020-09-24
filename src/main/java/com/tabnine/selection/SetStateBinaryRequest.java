package com.tabnine.selection;

import com.tabnine.binary.BinaryRequest;
import org.jetbrains.annotations.NotNull;

public class SetStateBinaryRequest implements BinaryRequest<SetStateRequest, SetStateResponse> {
    private final SetStateRequest setStateRequest;

    public SetStateBinaryRequest(SetStateRequest setStateRequest) {
        this.setStateRequest = setStateRequest;
    }

    public Class<SetStateResponse> response() {
        return SetStateResponse.class;
    }

    public boolean validate(@NotNull SetStateResponse response) {
        return response.getResult() != null;
    }

    public SetStateRequest serialize(int correlationId) {
        setStateRequest.setCorrelationId(correlationId);
        return setStateRequest;
    }
}
