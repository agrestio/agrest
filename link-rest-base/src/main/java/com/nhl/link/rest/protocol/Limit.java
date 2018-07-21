package com.nhl.link.rest.protocol;

/**
 * Represents 'limit' LinkRest protocol parameter
 *
 * @since 2.13
 */
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
