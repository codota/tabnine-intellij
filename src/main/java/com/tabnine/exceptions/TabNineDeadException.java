package com.tabnine.exceptions;

public class TabNineDeadException extends Exception {
    public TabNineDeadException() {
        super();
    }

    public TabNineDeadException(Exception e) {
        super(e);
    }

    public TabNineDeadException(String message) {
        super(message);
    }
}
