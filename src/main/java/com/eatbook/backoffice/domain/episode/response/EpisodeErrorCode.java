package com.eatbook.backoffice.domain.episode.response;

import com.eatbook.backoffice.global.response.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EpisodeErrorCode implements StatusCode{
    EPISODE_TITLE_DUPLICATED("EPISODE_TITLE_DUPLICATED", "이미 존재하는 에피소드 제목입니다."),
    EPISODE_NOT_FOUND("EPISODE_NOT_FOUND", "존재하지 않는 에피소드입니다."),
    EPISODE_NOT_EXIST("EPISODE_NOT_EXIST", "존재하지 않는 에피소드입니다."),
    ;

    private final String code;
    private final String message;
}