package com.tabnine.exceptions;

public class TabNineInvalidResponseException extends Exception {
    public TabNineInvalidResponseException() {
        super();
    }

    public TabNineInvalidResponseException(Exception e) {
        super(e);
    }
}
