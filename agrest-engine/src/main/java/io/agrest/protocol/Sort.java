package io.agrest.protocol;

import java.util.Objects;

/**
 * Represents 'sort' Agrest protocol parameter.
 *
 * @since 2.13
 */
public class Sort {

    private final String path;
    private final Direction direction;

    public Sort(String path) {
        this(path, Direction.asc);
    }

    public Sort(String path, Direction direction) {
        this.path = Objects.requireNonNull(path);
        this.direction = Objects.requireNonNull(direction);
    }

    public String getPath() {
        return path;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sort sort = (Sort) o;
        return path.equals(sort.path) &&
                direction == sort.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, direction);
    }

    @Override
    public String toString() {
        return "order by " + path + " " + direction;
    }
}
