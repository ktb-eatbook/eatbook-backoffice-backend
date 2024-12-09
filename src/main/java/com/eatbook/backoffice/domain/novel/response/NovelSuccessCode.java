package com.eatbook.backoffice.domain.novel.response;

import com.eatbook.backoffice.global.response.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NovelSuccessCode implements StatusCode {
    NOVEL_CREATED("NOVEL_CREATED", "소설이 성공적으로 생성되었습니다."),
    GET_NOVEL_LIST("GET_NOVEL_LIST", "소설 목록을 성공적으로 조회했습니다."),
    GET_NOVEL_DETAIL("GET_NOVEL_DETAIL", "소설 상세 정보를 성공적으로 조회했습니다."),
    GET_NOVEL_COMMENT_LIST("GET_NOVEL_COMMENT_LIST", "소설 댓글 목록을 성공적으로 조회했습니다."),
    GET_NOVEL_EPISODE_LIST("GET_NOVEL_EPISODE_LIST", "소설 에피소드 목록을 성공적으로 조회했습니다."),
    SEARCH_NOVELS("SEARCH_NOVELS", "소설을 성공적으로 검색했습니다."),
    ;

    private final String code;
    private final String message;
}
