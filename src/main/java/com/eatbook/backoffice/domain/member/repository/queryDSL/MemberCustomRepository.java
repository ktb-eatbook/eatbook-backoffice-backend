package com.eatbook.backoffice.domain.member.repository.queryDSL;

import com.eatbook.backoffice.domain.member.dto.MemberListResponse;
import org.springframework.data.domain.Pageable;

public interface MemberCustomRepository {
    MemberListResponse findMembers(Pageable pageable);
}