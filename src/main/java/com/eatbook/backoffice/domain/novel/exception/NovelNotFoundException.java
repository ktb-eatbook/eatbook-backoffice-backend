package com.eatbook.backoffice.domain.novel.exception;

import com.eatbook.backoffice.global.exception.exceptions.BusinessException;
import com.eatbook.backoffice.global.response.StatusCode;

public class NovelNotFoundException extends BusinessException {
    public NovelNotFoundException(StatusCode code) {
        super(code);
    }
}
