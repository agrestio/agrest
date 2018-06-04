package com.nhl.link.rest.constraints;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import org.apache.cayenne.exp.Expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A view of an LrEntity after applying request constraints.
 *
 * @since 2.4
 */
public class ConstrainedLrEntity<T> {

    private static final String[] QUERY_PARAMS = {"mapBy", "sort", "cayenneExp", "start", "limit", "exclude", "include"};

    private boolean idIncluded;
    private Collection<String> attributes;
    private Collection<String> queryParams;
    private Map<String, ConstrainedLrEntity<?>> children;
    private Expression qualifier;
    private LrEntity<T> entity;

    public ConstrainedLrEntity(LrEntity<T> entity) {

        if (entity == null) {
            throw new NullPointerException("Null entity");
        }

        this.idIncluded = false;
        this.entity = entity;
        this.children = new HashMap<>();

        // using HashSet, as we'll need fast 'contains' calls on attributes
        this.attributes = new HashSet<>();
        this.queryParams = new HashSet<>();
    }

    Collection<String> getAttributes() {
        return attributes;
    }

    Collection<String> getQueryParams() {
        return queryParams;
    }

    Map<String, ConstrainedLrEntity<?>> getChildren() {
        return children;
    }

    public LrEntity<T> getEntity() {
        return entity;
    }

    public boolean isIdIncluded() {
        return idIncluded;
    }

    public boolean hasAttribute(String name) {
        return attributes.contains(name);
    }

    /**
     * @since 2.13
     */
    public boolean hasQueryParam(String name) {
        return queryParams.contains(name.trim());
    }

    public ConstrainedLrEntity getChild(String name) {
        return children.get(name);
    }

    public boolean hasChild(String name) {
        return children.containsKey(name);
    }

    public Expression getQualifier() {
        return qualifier;
    }

    void excludeProperties(String... attributesOrRelationships) {
        if (attributesOrRelationships != null) {
            for (String name : attributesOrRelationships) {

                if (!attributes.remove(name)) {
                    children.remove(name);
                }
            }
        }
    }

    void excludeAllAttributes() {
        attributes.clear();
    }

    /**
     * @since 2.13
     */
    void excludeAllQueryParams() {
        queryParams.clear();
    }

    void excludeAllChildren() {
        children.clear();
    }

    void includeAttributes(String... attributes) {
        if (attributes != null) {

            for (String a : attributes) {
                this.attributes.add(a);
            }
        }
    }

    public void includeAllAttributes() {
        for (LrAttribute a : entity.getAttributes()) {
            this.attributes.add(a.getName());
        }
    }

    /**
     * @since 2.13
     */
    void includeQueryParams(String... queryParams) {
        if (queryParams != null) {

            for (String q : queryParams) {
                this.queryParams.add(q);
            }
        }
    }

    /**
     * @since 2.13
     */
    public void includeAllQueryParams() {
        includeQueryParams(QUERY_PARAMS);
    }

    void includeId(boolean include) {
        this.idIncluded = include;
    }

    void andQualifier(Expression qualifier) {
        if (this.qualifier == null) {
            this.qualifier = qualifier;
        } else {
            this.qualifier = this.qualifier.andExp(qualifier);
        }
    }

    void orQualifier(Expression qualifier) {

        if (this.qualifier == null) {
            this.qualifier = qualifier;
        } else {
            this.qualifier = this.qualifier.orExp(qualifier);
        }
    }
}
