package io.agrest.protocol;

/**
 * Represents 'limit' LinkRest protocol parameter.
 *
 * @since 2.13
 */
public class Limit {

    private int value;

    public Limit(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
