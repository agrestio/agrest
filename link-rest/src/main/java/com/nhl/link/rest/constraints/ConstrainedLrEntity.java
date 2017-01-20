package com.nhl.link.rest.constraints;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.parser.PathConstants;
import org.apache.cayenne.exp.Expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A view of an LrEntity after applying request constraints.
 *
 * @since 2.4
 */
public class ConstrainedLrEntity<T> implements  ConstraintVisitor {

    private boolean idIncluded;
    private Collection<String> attributes;
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
    }

    Collection<String> getAttributes() {
        return attributes;
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

    public ConstrainedLrEntity getChild(String name) {
        return children.get(name);
    }

    public boolean hasChild(String name) {
        return children.containsKey(name);
    }

    public Expression getQualifier() {
        return qualifier;
    }

    @Override
    public void visitExcludePropertiesConstraint(String... attributesOrRelationships) {
        if (attributesOrRelationships != null) {
            for (String name : attributesOrRelationships) {

                if (!attributes.remove(name)) {
                    children.remove(name);
                }
            }
        }
    }

    @Override
    public void visitExcludeAllAttributesConstraint() {
        attributes.clear();
    }

    @Override
    public void visitExcludeAllChildrenConstraint() {
        children.clear();
    }

    @Override
    public void visitIncludeAttributesConstraint(String... attributes) {
        if (attributes != null) {

            for (String a : attributes) {
                this.attributes.add(a);
            }
        }
    }

    @Override
    public void visitIncludeAllAttributesConstraint() {
        for (LrAttribute a : entity.getAttributes()) {
            this.attributes.add(a.getName());
        }
    }

    @Override
    public void visitIncludeIdConstraint(boolean include) {
        this.idIncluded = include;
    }

    @Override
    public void visitAndQualifierConstraint(Expression qualifier) {
        if (this.qualifier == null) {
            this.qualifier = qualifier;
        } else {
            this.qualifier = this.qualifier.andExp(qualifier);
        }
    }

    @Override
    public void visitOrQualifierConstraint(Expression qualifier) {

        if (this.qualifier == null) {
            this.qualifier = qualifier;
        } else {
            this.qualifier = this.qualifier.orExp(qualifier);
        }
    }

    @Override
    public ConstrainedLrEntity subtreeVisitor(String path) {

        StringTokenizer segments = new StringTokenizer(path, Character.toString(PathConstants.DOT));

        ConstrainedLrEntity c = this;
        while (segments.hasMoreTokens()) {
            c = ensurePath(c, segments.nextToken());
        }

        return c;
    }

    private ConstrainedLrEntity ensurePath(ConstrainedLrEntity parent, String pathSegment) {

        LrRelationship relationship = parent.entity.getRelationship(pathSegment);

        if (relationship == null) {
            throw new IllegalArgumentException("Path contains non-relationship component: " + pathSegment);
        }

        ConstrainedLrEntity child = parent.getChild(relationship.getName());
        if (child == null) {
            LrEntity<?> targetEntity = relationship.getTargetEntity();
            child = new ConstrainedLrEntity(targetEntity);
            parent.children.put(relationship.getName(), child);
        }

        return child;
    }

}
