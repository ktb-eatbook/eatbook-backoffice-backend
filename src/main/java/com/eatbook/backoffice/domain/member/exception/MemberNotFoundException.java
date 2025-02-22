package com.eatbook.backoffice.domain.member.exception;

import com.eatbook.backoffice.global.exception.exceptions.BusinessException;
import com.eatbook.backoffice.global.response.StatusCode;

public class MemberNotFoundException extends BusinessException {
    public MemberNotFoundException(StatusCode code) {
        super(code);
    }
}
