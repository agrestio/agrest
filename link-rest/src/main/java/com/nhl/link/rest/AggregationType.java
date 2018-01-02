package com.nhl.link.rest;

public enum AggregationType {

    AVERAGE("avg"),

    SUM("sum"),

    MINIMUM("min"),

    MAXIMUM("max");

    private String functionName;

    AggregationType(String functionName) {
        this.functionName = functionName;
    }

    public String functionName() {
        return functionName;
    }
}
