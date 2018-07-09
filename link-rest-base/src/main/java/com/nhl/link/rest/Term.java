package com.nhl.link.rest;

public enum Term {
    INCLUDE("include"),
    EXCLUDE("exclude"),
    START("start"),
    LIMIT("limit"),
    CAYENNE_EXP("cayenneExp"),
    MAP_BY("mapBy"),
    SORT("sort"),
    DIR("dir");

    private String value;
    Term (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
