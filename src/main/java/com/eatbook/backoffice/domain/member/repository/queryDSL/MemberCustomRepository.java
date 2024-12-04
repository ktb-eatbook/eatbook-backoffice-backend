package com.eatbook.backoffice.domain.member.repository.queryDSL;

import com.eatbook.backoffice.domain.member.dto.MemberListResponse;
import com.eatbook.backoffice.entity.constant.Role;
import org.springframework.data.domain.Pageable;

public interface MemberCustomRepository {
    MemberListResponse findMembers(Pageable pageable, Role role);
}