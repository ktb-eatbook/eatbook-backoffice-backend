package com.eatbook.backoffice.domain.member.controller;

import com.eatbook.backoffice.domain.member.dto.MemberListResponse;
import com.eatbook.backoffice.domain.member.service.MemberService;
import com.eatbook.backoffice.global.response.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.eatbook.backoffice.domain.member.response.MemberSuccessCode.GET_MEMBER_LIST;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class MemberController {
    private final MemberService memberService;

    /**
     * 제공된 조건에 따라 페이지별 멤버 목록을 검색합니다.
     *
     * @param page 검색할 페이지 번호 (양의 정수여야 함).
     * @param size 페이지당 멤버 수 (양의 정수여야 함).
     * @param sortField 멤버를 정렬할 필드 (기본값은 'id').
     * @param sortDirection 정렬 방향 (기본값은 'asc').
     * @return 멤버 목록을 포함하는 {@link ResponseEntity}
     */
    @GetMapping("/members")
    public ResponseEntity<ApiResponse> getMemberList(@RequestParam(name = "page") @Min(1) final int page,
                                                     @RequestParam(name = "size") @Min(1) final int size,
                                                     @RequestParam(name = "sortField", defaultValue = "id") final String sortField,
                                                     @RequestParam(name = "sortDirection", defaultValue = "asc") final String sortDirection) {
        MemberListResponse memberList = memberService.getMemberList(page, size, sortField, sortDirection);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(GET_MEMBER_LIST, memberList));
    }
}
