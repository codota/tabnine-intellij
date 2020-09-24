package com.tabnine.selection;

import com.google.gson.annotations.SerializedName;

public class SetStateRequestContentInner {
    @SerializedName(value = "Selection")
    public SelectionRequest selection;

    public SetStateRequestContentInner() {
    }

    public SetStateRequestContentInner(SelectionRequest selection) {
        this.selection = selection;
    }
}
