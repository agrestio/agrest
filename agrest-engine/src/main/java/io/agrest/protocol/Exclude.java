package io.agrest.protocol;

/**
 * Represents 'exclude' Agrest protocol parameter.
 *
 * @since 2.13
 */
public class Exclude {

    private final String path;

    public Exclude(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "exclude " + path;
    }
}
