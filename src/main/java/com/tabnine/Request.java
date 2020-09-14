package com.tabnine;

interface Request<T> {
    String name();

    Class<T> response();

    boolean validate(T response);
}
