package com.eatbook.backoffice.domain.episode.response;

import com.eatbook.backoffice.global.response.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EpisodeSuccessCode implements StatusCode {
    EPISODE_CREATED("EPISODE_CREATED", "에피소드가 성공적으로 생성되었습니다."),
    EPISODE_UPDATED("EPISODE_UPDATED", "에피소드가 성공적으로 수정되었습니다."),
    EPISODE_DELETED("EPISODE_DELETED", "에피소드가 성공적으로 삭제되었습니다."),
    EPISODE_FETCHED("EPISODE_FETCHED", "에피소드 상세 정보를 성공적으로 조회했습니다.")
    ;

    private final String code;
    private final String message;
}