package com.tabnine.contracts;

import com.tabnine.binary.BinaryResponse;

public class AutocompleteResponse implements BinaryResponse {
    public String old_prefix;
    public ResultEntry[] results;
    public String[] user_message;
    public Integer correlation_id;

    @Override
    public Integer getCorrelationId() {
        return correlation_id;
    }
}
