package com.tabnine.lifecycle;

import com.google.gson.annotations.SerializedName;
import com.tabnine.selections.BinaryResponse;

public class DisableResponse implements BinaryResponse {
    @SerializedName(value = "correlation_id")
    public Integer correlationId;

    @Override
    public Integer getCorrelationId() {
        return correlationId;
    }
}
