package com.eatbook.backoffice.domain.novel.exception;

import com.eatbook.backoffice.global.exception.exceptions.BusinessException;
import com.eatbook.backoffice.global.response.StatusCode;

public class PageOutOfBoundException  extends BusinessException {
    public PageOutOfBoundException(StatusCode code) {
        super(code);
    }
}
