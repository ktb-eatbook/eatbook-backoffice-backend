package com.eatbook.backoffice.domain.episode.exception;

import com.eatbook.backoffice.global.exception.exceptions.BusinessException;
import com.eatbook.backoffice.global.response.StatusCode;

public class EpisodeAlreadyExistsException extends BusinessException {
    public EpisodeAlreadyExistsException(StatusCode code) {
        super(code);
    }
}
