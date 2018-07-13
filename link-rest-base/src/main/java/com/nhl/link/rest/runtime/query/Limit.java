package com.nhl.link.rest.runtime.query;

public class Limit {
    private static final String LIMIT = "limit";

    private Integer value;

    public Limit(Integer value) {
        this.value = value;
    }

    public static String getName() {
        return LIMIT;
    }

    public Integer getValue() {
        return value;
    }
}
