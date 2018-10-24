package io.agrest.protocol;

/**
 * Represents 'start' Agrest protocol parameter.
 *
 * @since 2.13
 */
public class Start {

    private int value;

    public Start(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
