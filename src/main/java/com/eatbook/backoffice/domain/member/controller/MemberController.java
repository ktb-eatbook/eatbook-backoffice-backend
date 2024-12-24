package com.eatbook.backoffice.domain.member.controller;

import com.eatbook.backoffice.domain.member.dto.MemberListResponse;
import com.eatbook.backoffice.domain.member.service.MemberService;
import com.eatbook.backoffice.entity.constant.Role;
import com.eatbook.backoffice.entity.constant.SortDirection;
import com.eatbook.backoffice.entity.constant.SortField;
import com.eatbook.backoffice.global.response.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.eatbook.backoffice.domain.member.response.MemberSuccessCode.*;

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
                                                     @RequestParam(name = "role", required = false) final Role role,
                                                     @RequestParam(name = "sortField", defaultValue = "ID") final SortField sortField,
                                                     @RequestParam(name = "sortDirection", defaultValue = "ASC") final SortDirection sortDirection) {
        MemberListResponse memberList = memberService.getMemberList(page, size, role, sortField, sortDirection);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(GET_MEMBER_LIST, memberList));
    }

    @PutMapping("/members/{memberId}/roles/{role}")
    public ResponseEntity<ApiResponse> updateMemberRole(
                                                        @PathVariable("memberId") String memberId,
                                                        @PathVariable("role") String role) {
        memberService.updateMemberRole(memberId, role);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(UPDATE_MEMBER_ROLE));
    }

    /**
     * 지정된 회원 ID에 해당하는 회원을 삭제합니다.
     *
     * @param memberId 삭제할 회원의 ID.
     * @return 삭제 성공 메시지를 포함하는 {@link ResponseEntity}
     */
    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<ApiResponse> deleteMember(@PathVariable("memberId") String memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(DELETE_MEMBER));
    }
}
