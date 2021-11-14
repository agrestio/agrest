package io.agrest.base.protocol;

import java.util.Objects;

/**
 * Represents 'sort' Agrest protocol parameter.
 *
 * @since 2.13
 */
public class Sort {

    private final String property;
    private final Dir direction;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sort sort = (Sort) o;
        return property.equals(sort.property) &&
                direction == sort.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, direction);
    }
}
