package com.nhl.link.rest.runtime.query;

public class Start {
    private static final String START = "start";

    private Integer value;

    public Start(Integer value) {
        this.value = value;
    }

    public static String getName() {
        return START;
    }

    public Integer getValue() {
        return value;
    }
}
