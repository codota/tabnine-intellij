package com.tabnine.selection;

import com.google.gson.annotations.SerializedName;
import com.tabnine.binary.BinaryResponse;

public class SetStateResponse implements BinaryResponse {
    private String result;
    @SerializedName(value = "correlation_id")
    private Integer correlationId;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public Integer getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(Integer correlationId) {
        this.correlationId = correlationId;
    }
}
