package com.tabnine.lifecycle;

import com.tabnine.binary.exceptions.TabNineInvalidResponseException;
import com.tabnine.selections.BinaryRequest;
import org.jetbrains.annotations.NotNull;

import static com.tabnine.general.StaticConfig.wrapWithBinaryRequest;
import static java.util.Collections.singletonMap;

public class DisableRequest implements BinaryRequest<DisableResponse> {
    public static final String BACKWARD_COMPATIABLE_RESPONSE = "null";

    @Override
    public Class<DisableResponse> response() {
        return DisableResponse.class;
    }

    @Override
    public Object serialize(int correlationId) {
        return wrapWithBinaryRequest(singletonMap("Deactivate", singletonMap("correlation_id", correlationId)));
    }

    @Override
    public boolean validate(@NotNull DisableResponse response) {
        return true;
    }

    @Override
    public boolean shouldBeAllowed(TabNineInvalidResponseException e) {
        return e.getRawResponse().filter(BACKWARD_COMPATIABLE_RESPONSE::equals).isPresent();
    }
}
