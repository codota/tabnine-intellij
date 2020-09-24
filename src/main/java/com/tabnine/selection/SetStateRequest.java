package com.tabnine.selection;

import com.google.gson.annotations.SerializedName;

public class SetStateRequest {
    @SerializedName(value = "SetState")
    private SetStateRequestContent content;
    private Integer correlationId;

    public SetStateRequest() {
    }

    public SetStateRequest(SetStateRequestContent content) {
        this.content = content;
    }

    public SetStateRequestContent getContent() {
        return content;
    }

    public void setContent(SetStateRequestContent content) {
        this.content = content;
    }

    public Integer getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(Integer correlationId) {
        this.correlationId = correlationId;
    }
}
