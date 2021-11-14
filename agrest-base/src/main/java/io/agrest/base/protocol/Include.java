package io.agrest.base.protocol;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents 'include' Agrest protocol parameter.
 *
 * @since 2.13
 */
public class Include {

    private final Exp exp;
    private final List<Sort> orderings;
    private final String mapBy;
    private final String path;
    private final Integer start;
    private final Integer limit;

    public Include(String path) {
        this(path, null, Collections.emptyList(), null, null, null);
    }

    public Include(
            String path,
            Exp exp,
            List<Sort> orderings,
            String mapBy,
            Integer start,
            Integer limit) {

        this.path = Objects.requireNonNull(path);
        this.exp = exp;
        this.orderings = Objects.requireNonNull(orderings);
        this.mapBy = mapBy;
        this.start = start;
        this.limit = limit;
    }

    public String getMapBy() {
        return mapBy;
    }

    public String getPath() {
        return path;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getLimit() {
        return limit;
    }

    public Exp getExp() {
        return exp;
    }

    public List<Sort> getOrderings() {
        return orderings;
    }

    @Override
    public String toString() {
        return "[Include:" + path + "]";
    }
}
