package com.tabnine.selection;

import com.google.gson.annotations.SerializedName;

public class SetStateRequestContent {
    @SerializedName(value = "state_type")
    private SetStateRequestContentInner innerContent;

    public SetStateRequestContent() {
    }

    public SetStateRequestContent(SetStateRequestContentInner innerContent) {
        this.innerContent = innerContent;
    }

    public SetStateRequestContentInner getInnerContent() {
        return innerContent;
    }

    public void setInnerContent(SetStateRequestContentInner innerContent) {
        this.innerContent = innerContent;
    }
}
