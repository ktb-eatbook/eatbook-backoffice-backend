package com.eatbook.backoffice.domain.member.repository;

import com.eatbook.backoffice.domain.member.repository.queryDSL.MemberCustomRepository;
import com.eatbook.backoffice.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String>, MemberCustomRepository {
    Optional<Member> findMembersByEmail(String email);
}
