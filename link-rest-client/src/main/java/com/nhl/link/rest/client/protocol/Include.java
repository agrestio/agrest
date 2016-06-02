package com.nhl.link.rest.client.protocol;

import org.apache.cayenne.exp.Expression;

import java.util.Collection;

/**
 * @since 2.0
 */
public class Include {

    public static Include path(String path) {
        return new Include(path);
    }

    private String path;
    private LrRequest constraint;

    private Include(String path) {

        if (path == null) {
            throw new NullPointerException("Path");
        }
        this.path = path;
        constraint = new LrRequest();
    }

    public Include mapBy(String mapByPath) {
        constraint.mapBy(mapByPath);
        return this;
    }

    public Include cayenneExp(Expression cayenneExp) {
        constraint.cayenneExp(cayenneExp);
        return this;
    }

    public Include sort(String... properties) {
        constraint.sort(properties);
        return this;
    }

    public Include sort(Sort ordering) {
        constraint.sort(ordering);
        return this;
    }

    public Include start(long startIndex) {
        constraint.start(startIndex);
        return this;
    }

    public Include limit(long limit) {
        constraint.limit(limit);
        return this;
    }

    boolean isConstrained() {
        return constraint.hasAnyConstraints();
    }

    String getPath() {
        return path;
    }

    String getMapBy() {
        return constraint.getMapBy();
    }

    Expression getCayenneExp() {
        return constraint.getCayenneExp();
    }

    Long getStart() {
        return constraint.getStart();
    }

    Long getLimit() {
        return constraint.getLimit();
    }

    Collection<Sort> getOrderings() {
        return constraint.getOrderings();
    }
}
