package com.eatbook.backoffice.domain.episode.exception;

import com.eatbook.backoffice.global.exception.exceptions.BusinessException;
import com.eatbook.backoffice.global.response.StatusCode;

public class EpisodeNotFoundException extends BusinessException {
    public EpisodeNotFoundException(StatusCode code) {
        super(code);
    }
}
