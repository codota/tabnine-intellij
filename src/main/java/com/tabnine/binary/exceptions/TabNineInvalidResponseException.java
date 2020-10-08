package com.tabnine.binary.exceptions;

public class TabNineInvalidResponseException extends Exception {
    public TabNineInvalidResponseException() {
        super();
    }

    public TabNineInvalidResponseException(String s) {
        super(s);
    }

    public TabNineInvalidResponseException(String message, Exception e) {
        super(message, e);
    }
}
