package com.eatbook.backoffice.entity.constant;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

import java.util.Arrays;

import static com.eatbook.backoffice.entity.QMember.member;

public enum SortField {
    ID("id", member.id),
    NICKNAME("nickname", member.nickname),
    EMAIL("email", member.email),
    CREATED_AT("createdAt", member.createdAt);

    private final String fieldName;
    private final ComparableExpressionBase<?> expression;

    SortField(String fieldName, ComparableExpressionBase<?> expression) {
        this.fieldName = fieldName;
        this.expression = expression;
    }

    public static SortField from(String fieldName) {
        return Arrays.stream(values())
                .filter(field -> field.fieldName.equalsIgnoreCase(fieldName))
                .findFirst()
                .orElse(ID);
    }

    public OrderSpecifier<?> getOrderSpecifier(boolean isDesc) {
        return isDesc ? expression.desc() : expression.asc();
    }
}