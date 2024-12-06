package com.eatbook.backoffice.domain.member.service;

import com.eatbook.backoffice.domain.member.dto.MemberListResponse;
import com.eatbook.backoffice.domain.member.exception.InvalidRoleException;
import com.eatbook.backoffice.domain.member.repository.MemberRepository;
import com.eatbook.backoffice.entity.constant.Role;
import com.eatbook.backoffice.entity.constant.SortDirection;
import com.eatbook.backoffice.entity.constant.SortField;
import com.eatbook.backoffice.global.exception.exceptions.PageOutOfBoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.eatbook.backoffice.domain.member.response.MemberErrorCode.INVALID_ROLE;
import static com.eatbook.backoffice.global.response.GlobalErrorCode.PAGE_OUT_OF_BOUNDS;

/**
 * 멤버 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 이 클래스는 멤버 목록을 페이징 처리하여 조회하는 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 멤버 목록을 페이징 처리하여 조회합니다.
     * 요청된 페이지 번호가 유효한 범위 내에 있는지 확인하고, 범위를 초과한 경우 예외를 발생시킵니다.
     *
     * @param page 요청된 페이지 번호 (1부터 시작하는 인덱스)
     * @param size 페이지당 멤버 수
     * @param sortField 정렬할 필드
     * @param sortDirection 정렬 방향 ("ASC" 또는 "DESC")
     * @return 요청된 페이지에 해당하는 멤버 목록을 담은 {@link MemberListResponse} 객체
     * @throws PageOutOfBoundException 요청한 페이지 번호가 전체 페이지 수를 초과할 경우 발생
     */
    @Transactional(readOnly = true)
    public MemberListResponse getMemberList(int page, int size, Role role, SortField sortField, SortDirection sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.name()), sortField.name());
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        MemberListResponse members = memberRepository.findMembers(pageable, role);

        validatePageRequest(pageable, members.totalElements());

        return members;
    }

    /**
     * 멤버의 역할을 업데이트합니다.
     * 이 메서드는 관리자 권한을 가진 멤버만 호출할 수 있으며,
     * 요청된 멤버 ID에 해당하는 멤버의 역할을 새로운 역할로 업데이트합니다.
     *
     * @param memberId 역할을 업데이트할 멤버 ID
     * @param role 새로운 역할 (ADMIN, USER)
     * @throws InvalidRoleException 요청된 역할이 유효하지 않은 경우
     */
    @Transactional
    public void updateMemberRole(String memberId, String role) {
        Role newRole = validateRole(role);

        memberRepository.updateMemberRole(memberId, newRole);
    }

    /**
     * 주어진 역할 문자열을 검증하고, 해당하는 {@link Role} 열거형 값을 반환합니다.
     * 역할 문자열이 유효한 열거형 값이 아니면 {@link InvalidRoleException}이 발생합니다.
     *
     * @param role 검증할 역할 문자열.
     * @return 해당하는 {@link Role} 열거형 값.
     * @throws InvalidRoleException 역할 문자열이 유효한 열거형 값이 아닌 경우.
     */
    private Role validateRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRoleException(INVALID_ROLE);
        }
    }

    /**
     * 요청된 페이지 번호가 전체 페이지 수를 초과하는지 검증합니다.
     *
     * @param pageable 요청된 페이지 정보가 담긴 {@link Pageable} 객체
     * @param totalElements 총 멤버 수
     * @throws PageOutOfBoundException 요청한 페이지 번호가 전체 페이지 수를 초과할 경우 발생
     */
    private void validatePageRequest(Pageable pageable, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        if (pageable.getPageNumber() >= totalPages) {
            throw new PageOutOfBoundException(PAGE_OUT_OF_BOUNDS);
        }
    }
}