package io.agrest.constraints;

import io.agrest.base.protocol.Exp;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A view of an AgEntity after applying request constraints.
 *
 * @since 2.4
 */
public class ConstrainedAgEntity<T> {

    private boolean idIncluded;
    private Collection<String> attributes;
    private Map<String, ConstrainedAgEntity<?>> children;
    private Exp qualifier;
    private AgEntity<T> entity;

    public ConstrainedAgEntity(AgEntity<T> entity) {

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

    Map<String, ConstrainedAgEntity<?>> getChildren() {
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

    public ConstrainedAgEntity getChild(String name) {
        return children.get(name);
    }

    public boolean hasChild(String name) {
        return children.containsKey(name);
    }

    /**
     * @deprecated since 4.7. For server-side filtering use "Ag.request().andExp()"
     */
    @Deprecated
    public Exp getQualifier() {
        return qualifier;
    }

    void excludeProperties(String... attributesOrRelationships) {
        if (attributesOrRelationships != null) {
            for (String name : attributesOrRelationships) {

                if (!attributes.remove(name)) {
                    // TODO: should we log removal like we do in EntityConstraintHandler? Unlike in
                    //  EntityConstraintHandler we can't check if the removed attribute is "default" though
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

    @Deprecated
    void setQualifier(Exp qualifier) {
        this.qualifier = qualifier;
    }
}
