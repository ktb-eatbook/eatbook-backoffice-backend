package com.eatbook.backoffice.domain.member.repository.queryDSL;

import com.eatbook.backoffice.domain.member.dto.MemberInfo;
import com.eatbook.backoffice.domain.member.dto.MemberListResponse;
import com.eatbook.backoffice.entity.constant.Role;
import com.eatbook.backoffice.entity.constant.SortField;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.eatbook.backoffice.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public MemberListResponse findMembers(Pageable pageable, Role role) {

        OrderSpecifier<?> orderSpecifier = toOrderSpecifier(pageable.getSort());

        List<MemberInfo> memberInfoList = jpaQueryFactory
                .select(Projections.constructor(MemberInfo.class,
                        member.id,
                        member.role.stringValue(),
                        member.nickname,
                        member.profileImageUrl,
                        member.email,
                        member.createdAt,
                        member.updatedAt,
                        member.deletedAt
                ))
                .from(member)
                .where(role == null ? null : member.role.eq(role))
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long totalElements = jpaQueryFactory
                .select(member.count())
                .from(member)
                .where(
                        role == null ? null : member.role.eq(role)
                )
                .fetchOne();

        Page<MemberInfo> page = new PageImpl<>(memberInfoList, pageable, totalElements);

        return MemberListResponse.of(
                (int) page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber() + 1,
                page.getSize(),
                page.getContent()
        );
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort sort) {
        for (Sort.Order order : sort) {
            SortField sortField = SortField.from(order.getProperty());
            boolean isDesc = order.getDirection().isDescending();
            return sortField.getOrderSpecifier(isDesc);
        }
        return SortField.ID.getOrderSpecifier(false);
    }
}