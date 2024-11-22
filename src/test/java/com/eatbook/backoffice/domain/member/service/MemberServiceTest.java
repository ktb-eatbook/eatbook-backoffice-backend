package com.eatbook.backoffice.domain.member.service;

import com.eatbook.backoffice.domain.member.dto.MemberInfo;
import com.eatbook.backoffice.domain.member.dto.MemberListResponse;
import com.eatbook.backoffice.domain.member.repository.MemberRepository;
import com.eatbook.backoffice.global.exception.exceptions.PageOutOfBoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.eatbook.backoffice.domain.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void should_ReturnMemberList_When_ValidParametersProvided() {
        // Given
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(defaultSortField).ascending());

        List<MemberInfo> sampleMembers = createSampleMembers();
        MemberListResponse mockResponse = createMemberListResponse(page, size, sampleMembers);

        when(memberRepository.findMembers(pageable)).thenReturn(mockResponse);

        // When
        MemberListResponse result = memberService.getMemberList(page, size, defaultSortField, defaultSortDirection);

        // Then
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    void should_ThrowPageOutOfBoundException_When_PageExceedsTotalPages() {
        // Given
        List<MemberInfo> sampleMembers = createSampleMembers();
        MemberListResponse mockResponse = createMemberListResponse(page, size, sampleMembers);

        when(memberRepository.findMembers(org.mockito.ArgumentMatchers.any())).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> memberService.getMemberList(invalidPage, size, defaultSortField, defaultSortDirection))
                .isInstanceOf(PageOutOfBoundException.class)
                .hasMessage("요청된 페이지가 총 페이지 수를 초과했습니다.");
    }
}