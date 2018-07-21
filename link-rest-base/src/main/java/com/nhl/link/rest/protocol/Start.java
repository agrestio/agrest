package com.nhl.link.rest.protocol;

/**
 * Represents 'start' LinkRest protocol parameter.
 *
 * @since 2.13
 */
public class Start {

    public static final String START = "start";

    private int value;

    public Start(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
