package com.tabnine.selections;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.tabnine.general.StaticConfig.SET_STATE_RESPONSE_RESULT_STRING;
import static com.tabnine.general.StaticConfig.wrapWithBinaryRequest;
import static java.util.Collections.singletonMap;

public class SetStateBinaryRequest implements BinaryRequest<Map<String, Object>, SetStateBinaryResponse> {
    private SelectionRequest selectionRequest;

    public SetStateBinaryRequest(SelectionRequest selectionRequest) {
        this.selectionRequest = selectionRequest;
    }

    @Override
    public Class<SetStateBinaryResponse> response() {
        return SetStateBinaryResponse.class;
    }

    @Override
    public Map<String, Object> serialize(int correlationId) {
        Map<String, Object> map = new HashMap<>();

        map.put("state_type", singletonMap("Selection", selectionRequest));
        map.put("correlation_id",correlationId);

        return wrapWithBinaryRequest(singletonMap("SetState", map));
    }

    @Override
    public boolean validate(@NotNull SetStateBinaryResponse response) {
        return SET_STATE_RESPONSE_RESULT_STRING.equals(response.getResult());
    }
}
