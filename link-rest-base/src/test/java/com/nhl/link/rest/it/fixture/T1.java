package com.nhl.link.rest.it.fixture;

public class T1 {
    public static final String P_BOOLEAN = "boolean";
    public static final String P_INTEGER = "integer";
    public static final String P_STRING = "string";

    private Boolean booleanProperty;
    private Integer integerProperty;
    private String stringProperty;

    public Boolean isBoolean() {
        return booleanProperty;
    }

    public void setBoolean(Boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public Integer getInteger() {
        return integerProperty;
    }

    public void setInteger(Integer pInteger) {
        this.integerProperty = pInteger;
    }

    public String getString() {
        return stringProperty;
    }

    public void setString(String pString) {
        this.stringProperty = pString;
    }
}
