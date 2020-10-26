package com.tabnine.binary.requests.selection;

import com.tabnine.binary.BinaryResponse;

public class SetStateBinaryResponse implements BinaryResponse {
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
