package com.nhl.link.rest.client;

import org.apache.cayenne.exp.Expression;

import java.util.Collection;
import java.util.Collections;

public class IncludeBuilder {

    public static IncludeBuilder path(String path) {
        return new IncludeBuilder(path);
    }

    private String path;
    private ConstraintBuilder constraintBuilder;

    private IncludeBuilder(String path) {

        if (path == null) {
            throw new NullPointerException("Path");
        }
        this.path = path;
        constraintBuilder = new ConstraintBuilder();
    }

    public IncludeBuilder mapBy(String mapByPath) {
        constraintBuilder.mapBy(mapByPath);
        return this;
    }

    public IncludeBuilder cayenneExp(Expression cayenneExp) {
        constraintBuilder.cayenneExp(cayenneExp);
        return this;
    }

    public IncludeBuilder sort(String... properties) {
        constraintBuilder.sort(properties);
        return this;
    }

    public IncludeBuilder sort(Sort ordering) {
        constraintBuilder.sort(ordering);
        return this;
    }

    public IncludeBuilder start(long startIndex) {
        constraintBuilder.start(startIndex);
        return this;
    }

    public IncludeBuilder limit(long limit) {
        constraintBuilder.limit(limit);
        return this;
    }

    public Include build() {

        final Constraint constraint;
        if (constraintBuilder.hasAnyConstraints()) {
            constraint = constraintBuilder.build();
        } else {
            constraint = null;
        }

        return new Include() {

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public boolean isConstrained() {
                return constraint != null;
            }

            @Override
            public String getMapBy() {
                return constraint == null? null : constraint.getMapBy();
            }

            @Override
            public Expression getCayenneExp() {
                return constraint == null? null : constraint.getCayenneExp();
            }

            @Override
            public Collection<Sort> getOrderings() {
                return constraint == null? Collections.emptyList() : constraint.getOrderings();
            }

            @Override
            public Long getStart() {
                return constraint == null? null : constraint.getStart();
            }

            @Override
            public Long getLimit() {
                return constraint == null? null : constraint.getLimit();
            }
        };
    }
}
