package com.nhl.link.rest.client;

import org.apache.cayenne.exp.Expression;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ConstraintBuilder {

    private String mapByPath;
    private Expression cayenneExp;
    private Map<String, Sort> orderings;
    private Long startIndex;
    private Long limit;
    
    private Set<String> excludes;
    private Map<String, Include> includeMap;

    public ConstraintBuilder mapBy(String mapByPath) {
        this.mapByPath = mapByPath;
        return this;
    }

    public ConstraintBuilder cayenneExp(Expression cayenneExp) {
        this.cayenneExp = cayenneExp;
        return this;
    }

    public ConstraintBuilder sort(String... properties) {

        if (properties != null) {
            for (String property : properties) {
                addSort(property, Sort.property(property));
            }
        }
        return this;
    }

    public ConstraintBuilder sort(Sort ordering) {

        if (ordering != null) {
            addSort(ordering.getPropertyName(), ordering);
        }
        return this;
    }

    private void addSort(String property, Sort sort) {

        if (orderings == null) {
            orderings = new HashMap<>();
        }
        orderings.put(property, sort);
    }

    public ConstraintBuilder start(long startIndex) {

        if (startIndex >= 0) {
            this.startIndex = startIndex;
        }
        return this;
    }

    public ConstraintBuilder limit(long limit) {

        if (limit > 0) {
            this.limit = limit;
        }
        return this;
    }
    
    public ConstraintBuilder exclude(String... excludePaths) {

        if (excludePaths != null) {
            if (excludes == null) {
                excludes = new HashSet<>();
            }
            for (String excludePath : excludePaths) {
                if (excludePath != null) {
                    excludes.add(excludePath);
                }
            }
        }
        return this;
    }
    
    public ConstraintBuilder include(String... includePaths) {

        if (includePaths != null) {
            for (String includePath : includePaths) {
                if (includePath != null) {
                    addInclude(includePath, IncludeBuilder.path(includePath).build());
                }
            }
        }
        return this;
    }

    public ConstraintBuilder include(Include include) {

        if (include != null) {
            addInclude(include.getPath(), include);
        }
        return this;
    }
    
    private void addInclude(String path, Include include) {

        if (includeMap == null) {
            includeMap = new HashMap<>();
        }
        includeMap.put(path, include);
    }

    public boolean hasAnyConstraints() {
        return mapByPath != null || cayenneExp != null || startIndex != null || limit != null ||
                (orderings != null && orderings.size() > 0) ||
                (excludes != null && excludes.size() > 0) ||
                (includeMap != null && includeMap.size() > 0);
    }

    public Constraint build() {

        if (!hasAnyConstraints()) {
            throw new LinkRestClientException("Can't build -- missing constraints");
        }

        return new Constraint() {

            @Override
            public String getMapBy() {
                return mapByPath;
            }

            @Override
            public Expression getCayenneExp() {
                return cayenneExp;
            }

            @Override
            public Collection<Sort> getOrderings() {
                return orderings == null? Collections.emptyList() : orderings.values();
            }

            @Override
            public Long getStart() {
                return startIndex;
            }

            @Override
            public Long getLimit() {
                return limit;
            }

            @Override
            public Collection<String> getExcludes() {
                return excludes == null? Collections.emptyList() : excludes;
            }

            @Override
            public Collection<Include> getIncludes() {
                return includeMap == null? Collections.emptyList() : includeMap.values();
            }
        };
    }
}
