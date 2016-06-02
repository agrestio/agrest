package com.nhl.link.rest.client;

import javax.ws.rs.client.WebTarget;

/**
 * @since 2.0
 */
public class TargetBuilder {

    private static final String MAP_BY = "mapBy";
    private static final String CAYENNE_EXP = "cayenneExp";
    private static final String START = "start";
    private static final String LIMIT = "limit";
    private static final String SORT = "sort";
    private static final String EXCLUDE = "exclude";
    private static final String INCLUDE = "include";

    public static TargetBuilder target(WebTarget target) {
        return new TargetBuilder(target);
    }

    private WebTarget target;
    private Constraint constraint;

    private TargetBuilder(WebTarget target) {

        if (target == null) {
            throw new NullPointerException("Target");
        }
        this.target = target;
    }

    public TargetBuilder constraint(Constraint constraint) {

        if (constraint != null) {
            this.constraint = constraint;
        }
        return this;
    }

    public WebTarget build() {

        WebTarget newTarget = target;
        if (constraint != null) {

            if (constraint.getMapBy() != null) {
                newTarget = newTarget.queryParam(MAP_BY, constraint.getMapBy());
            }
            if (constraint.getStart() != null) {
                newTarget = newTarget.queryParam(START, constraint.getStart());
            }
            if (constraint.getLimit() != null) {
                newTarget = newTarget.queryParam(LIMIT, constraint.getLimit());
            }
            for (String exclude : constraint.getExcludes()) {
                newTarget = newTarget.queryParam(EXCLUDE, exclude);
            }

            ConstraintEncoder encoder = ConstraintEncoder.encoder();
            if (constraint.getCayenneExp() != null) {
                newTarget = newTarget.queryParam(CAYENNE_EXP, encoder.encode(constraint.getCayenneExp()));
            }
            if (!constraint.getOrderings().isEmpty()) {
                newTarget = newTarget.queryParam(SORT, encoder.encode(constraint.getOrderings()));
            }
            for (Include include : constraint.getIncludes()) {
                newTarget = newTarget.queryParam(INCLUDE, encoder.encode(include));
            }
        }

        return newTarget;
    }
}
