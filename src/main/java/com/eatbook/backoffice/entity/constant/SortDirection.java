package com.eatbook.backoffice.entity.constant;

public enum SortDirection {
    ASC("ASC"),
    DESC("DESC");

    private final String value;

    SortDirection(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
