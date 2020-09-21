package com.tabnine.contracts;

public class AutocompleteResponse {
    public String old_prefix;
    public ResultEntry[] results;
    public String[] user_message;
    public Integer correlation_id;
}
