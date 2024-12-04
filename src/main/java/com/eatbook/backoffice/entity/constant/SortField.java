package com.eatbook.backoffice.entity.constant;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final Map<String, SortField> FIELD_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(
                            field -> field.fieldName.toLowerCase(),
                            Function.identity()
                    ));

    public static SortField from(String fieldName) {
        return FIELD_MAP.getOrDefault(
                fieldName != null ? fieldName.toLowerCase() : null,
                ID
        );
    }

    public OrderSpecifier<?> getOrderSpecifier(boolean isDesc) {
        return isDesc ? expression.desc() : expression.asc();
    }
}