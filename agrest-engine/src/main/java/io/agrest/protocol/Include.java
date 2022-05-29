package io.agrest.protocol;

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
    private final List<Sort> sorts;
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
            List<Sort> sorts,
            String mapBy,
            Integer start,
            Integer limit) {

        this.path = Objects.requireNonNull(path);
        this.exp = exp;
        this.sorts = Objects.requireNonNull(sorts);
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

    public List<Sort> getSorts() {
        return sorts;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #getSorts()}
     */
    @Deprecated
    public List<Sort> getOrderings() {
        return getSorts();
    }

    @Override
    public String toString() {
        return "include " + path;
    }
}
