package io.agrest.protocol;

import java.util.Objects;

/**
 * Represents 'sort' Agrest protocol parameter.
 *
 * @since 2.13
 */
public class Sort {

    private String property;
    private Dir direction;

    public Sort(String property) {
        this(property, Dir.ASC);
    }

    public Sort(String property, Dir direction) {
        this.property = Objects.requireNonNull(property);
        this.direction = Objects.requireNonNull(direction);
    }

    public String getProperty() {
        return property;
    }

    public Dir getDirection() {
        return direction;
    }
}
