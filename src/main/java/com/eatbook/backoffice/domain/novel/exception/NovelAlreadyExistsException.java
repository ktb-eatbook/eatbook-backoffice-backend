package com.eatbook.backoffice.domain.novel.exception;

import com.eatbook.backoffice.global.exception.exceptions.BusinessException;
import com.eatbook.backoffice.global.response.StatusCode;

public class NovelAlreadyExistsException extends BusinessException {
    public NovelAlreadyExistsException(StatusCode code) {
        super(code);
    }
}