package io.agrest.constraints;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import org.apache.cayenne.exp.Expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A view of an AgEntity after applying request constraints.
 *
 * @since 2.4
 */
public class ConstrainedLrEntity<T> {

    private boolean idIncluded;
    private Collection<String> attributes;
    private Map<String, ConstrainedLrEntity<?>> children;
    private Expression qualifier;
    private AgEntity<T> entity;

    public ConstrainedLrEntity(AgEntity<T> entity) {

        if (entity == null) {
            throw new NullPointerException("Null entity");
        }

        this.idIncluded = false;
        this.entity = entity;
        this.children = new HashMap<>();

        // using HashSet, as we'll need fast 'contains' calls on attributes
        this.attributes = new HashSet<>();
    }

    Collection<String> getAttributes() {
        return attributes;
    }

    Map<String, ConstrainedLrEntity<?>> getChildren() {
        return children;
    }

    public AgEntity<T> getEntity() {
        return entity;
    }

    public boolean isIdIncluded() {
        return idIncluded;
    }

    public boolean hasAttribute(String name) {
        return attributes.contains(name);
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
        for (AgAttribute a : entity.getAttributes()) {
            this.attributes.add(a.getName());
        }
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
