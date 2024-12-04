package com.eatbook.backoffice.domain.member.fixture;

import com.eatbook.backoffice.domain.member.dto.MemberInfo;
import com.eatbook.backoffice.domain.member.dto.MemberListResponse;
import com.eatbook.backoffice.entity.constant.Role;
import com.eatbook.backoffice.entity.constant.SortDirection;
import com.eatbook.backoffice.entity.constant.SortField;

import java.time.LocalDateTime;
import java.util.List;

public class MemberFixture {

    public static int page = 1;
    public static int size = 10;
    public static Role role = Role.MEMBER;
    public static int invalidPage = 10000;
    public static SortField defaultSortField = SortField.ID;
    public static SortDirection defaultSortDirection = SortDirection.ASC;



    public static MemberInfo createMemberInfo(String id, String role, String nickname, String profileImageUrl, String email) {
        return new MemberInfo(
                id,
                role,
                nickname,
                profileImageUrl,
                email,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    public static MemberListResponse createMemberListResponse(int page, int size, List<MemberInfo> memberList) {
        return MemberListResponse.of(
                memberList.size(),
                (int) Math.ceil((double) memberList.size() / size),
                page,
                size,
                memberList
        );
    }

    public static List<MemberInfo> createSampleMembers() {
        return List.of(
                createMemberInfo("f4bc9f37-6078-4ca3-b5e3-02899b178331", "USER", "lavin", "url1", "lavin@example.com"),
                createMemberInfo("5bfb595d-08fa-4173-9e7c-be2cd0ac9bbb", "ADMIN", "lily", "url2", "lily@example.com")
        );
    }
}
