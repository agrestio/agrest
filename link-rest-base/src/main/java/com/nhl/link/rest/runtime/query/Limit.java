package com.nhl.link.rest.runtime.query;

public class Limit {
    public static final String LIMIT = "limit";

    private int value;

    public Limit(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
