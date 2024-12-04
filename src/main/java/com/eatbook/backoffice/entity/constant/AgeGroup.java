package com.eatbook.backoffice.entity.constant;

import java.util.Arrays;

public enum AgeGroup {
    TWENTIES(20),
    THIRTIES(30),
    FORTIES(40),
    FIFTIES(50),
    SIXTIES(60),
    SEVENTIES(70),
    EIGHTIES(80),
    NINETIES(90);

    private final int value;

    AgeGroup(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AgeGroup fromValue(int value) {
        return Arrays.stream(AgeGroup.values())
                .filter(group -> group.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid age group value: " + value));
    }
}