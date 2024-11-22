package com.eatbook.backoffice.global.exception.exceptions;

import com.eatbook.backoffice.global.response.StatusCode;

public class PageOutOfBoundException extends BusinessException {
    public PageOutOfBoundException(StatusCode code) {
        super(code);
    }
}
