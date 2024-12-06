package com.eatbook.backoffice.domain.member.repository;

import com.eatbook.backoffice.domain.member.repository.queryDSL.MemberCustomRepository;
import com.eatbook.backoffice.entity.Member;
import com.eatbook.backoffice.entity.constant.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String>, MemberCustomRepository {
    Optional<Member> findMemberByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE Member m SET m.role = :role WHERE m.id = :id")
    void updateMemberRole(@Param("id") String id, @Param("role") Role role);
}
