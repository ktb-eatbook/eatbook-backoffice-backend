package com.eatbook.backoffice.domain.member.exception;

import com.eatbook.backoffice.global.exception.exceptions.BusinessException;
import com.eatbook.backoffice.global.response.StatusCode;

public class InvalidRoleException extends BusinessException {
    public InvalidRoleException(StatusCode code) {
        super(code);
    }
}
