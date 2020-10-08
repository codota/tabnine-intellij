package com.tabnine.binary.exceptions;

import java.util.Optional;

public class TabNineInvalidResponseException extends Exception {
    private final Optional<String> rawResponse;

    public TabNineInvalidResponseException() {
        super();
        this.rawResponse = Optional.empty();
    }

    public TabNineInvalidResponseException(String s) {
        super(s);
        this.rawResponse = Optional.empty();

    }

    public TabNineInvalidResponseException(String format, Exception e, String rawResponse) {
        super(format, e);
        this.rawResponse = Optional.ofNullable(rawResponse);
    }

    public Optional<String> getRawResponse() {
        return rawResponse;
    }
}
