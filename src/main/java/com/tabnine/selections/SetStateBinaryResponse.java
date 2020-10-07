package com.tabnine.selections;

import com.google.gson.annotations.SerializedName;

public class SetStateBinaryResponse implements BinaryResponse {
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
