package com.eatbook.backoffice.domain.novel.exception;

import com.eatbook.backoffice.global.exception.exceptions.BusinessException;
import com.eatbook.backoffice.global.response.StatusCode;

public class PresignedUrlGenerationException extends BusinessException {

    public PresignedUrlGenerationException(StatusCode code, String detailMessage) {
        super(detailMessage, code);
    }

    public PresignedUrlGenerationException(StatusCode code) {
        super(code);
    }

}
