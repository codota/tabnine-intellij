package com.tabnine.exceptions;

public class TabNineDeadException extends Exception {
    private String location = null;

    public TabNineDeadException() {
        super();
    }

    public TabNineDeadException(Exception e) {
        super(e);
    }

    public TabNineDeadException(String message) {
        super(message);
    }

    public TabNineDeadException(Exception e, String location) {
        super(e);
        this.location = location;
    }

    public String getLocation() {
        return location;
    }
}
