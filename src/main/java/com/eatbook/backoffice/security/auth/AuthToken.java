package com.eatbook.backoffice.security.auth;

public interface AuthToken <T> {
    boolean validate();
    T getData();
}
