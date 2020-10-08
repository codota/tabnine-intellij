package com.tabnine.lifecycle;

import com.tabnine.binary.exceptions.TabNineInvalidResponseException;
import com.tabnine.selections.BinaryRequest;
import org.jetbrains.annotations.NotNull;

import static com.tabnine.general.StaticConfig.wrapWithBinaryRequest;
import static java.util.Collections.singletonMap;

public class UninstallRequest implements BinaryRequest<UninstallResponse> {
    public static final String BACKWARD_COMPATIABLE_RESPONSE = "null";

    @Override
    public Class<UninstallResponse> response() {
        return UninstallResponse.class;
    }

    @Override
    public Object serialize(int correlationId) {
        return wrapWithBinaryRequest(singletonMap("Uninstalling", singletonMap("correlation_id", correlationId)));
    }

    @Override
    public boolean validate(@NotNull UninstallResponse response) {
        return true;
    }

    @Override
    public boolean shouldBeAllowed(TabNineInvalidResponseException e) {
        return e.getRawResponse().filter(BACKWARD_COMPATIABLE_RESPONSE::equals).isPresent();
    }
}
