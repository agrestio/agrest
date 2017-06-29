package com.nhl.link.rest;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.util.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A metadata object that describes a data structure of a given REST resource.
 * Connected ResourceEntities form a tree-like structure that usually overlays a
 * certain Cayenne mapping subgraph (unless this is a non-persistent entity),
 * filtering and extending its properties to describe the data structure to be
 * returned to the client.
 * <p>
 * ResourceEntity scope is usually a single request. It is built on the fly by
 * the framework or by the application code.
 */
public class ResourceEntity<T> {

    private boolean idIncluded;

    private LrEntity<T> lrEntity;
    private Map<String, LrAttribute> attributes;
    private Collection<String> defaultProperties;

    private String applicationBase;
    private String mapByPath;
    private ResourceEntity<?> mapBy;
    private Map<String, ResourceEntity<?>> children;
    private LrRelationship incoming;
    private Collection<Ordering> orderings;
    private Expression qualifier;
    private Map<String, EntityProperty> extraProperties;
    private int fetchOffset;
    private int fetchLimit;
    private boolean filtered;

    public ResourceEntity(LrEntity<T> lrEntity) {
        this.idIncluded = false;
        this.attributes = new HashMap<>();
        this.defaultProperties = new HashSet<>();
        this.children = new HashMap<>();
        this.orderings = new ArrayList<>(2);
        this.extraProperties = new HashMap<>();
        this.lrEntity = lrEntity;
    }

    public ResourceEntity(LrEntity<T> lrEntity, LrRelationship incoming) {
        this(lrEntity);
        this.incoming = incoming;
    }

    /**
     * @since 1.12
     */
    public LrEntity<T> getLrEntity() {
        return lrEntity;
    }

    public LrRelationship getIncoming() {
        return incoming;
    }

    public Expression getQualifier() {
        return qualifier;
    }

    /**
     * Resets the qualifier for the entity to a new one.
     *
     * @param qualifier a new qualifier expression. Can be null.
     * @since 2.7
     */
    public void setQualifier(Expression qualifier) {
        this.qualifier = qualifier;
    }

    public void andQualifier(Expression qualifier) {
        if (this.qualifier == null) {
            this.qualifier = qualifier;
        } else {
            this.qualifier = this.qualifier.andExp(qualifier);
        }
    }

    public Collection<Ordering> getOrderings() {
        return orderings;
    }

    /**
     * @since 1.12
     */
    public Map<String, LrAttribute> getAttributes() {
        return attributes;
    }

    /**
     * @since 1.5
     */
    public Collection<String> getDefaultProperties() {
        return defaultProperties;
    }

    /**
     * @since 1.5
     */
    public boolean isDefault(String propertyName) {
        return defaultProperties.contains(propertyName);
    }

    public Map<String, ResourceEntity<?>> getChildren() {
        return children;
    }

    /**
     * @since 1.1
     */
    public ResourceEntity<?> getChild(String name) {
        return children.get(name);
    }

    public Map<String, EntityProperty> getExtraProperties() {
        return extraProperties;
    }

    public boolean isIdIncluded() {
        return idIncluded;
    }

    public ResourceEntity<T> includeId(boolean include) {
        this.idIncluded = include;
        return this;
    }

    public ResourceEntity<T> includeId() {
        this.idIncluded = true;
        return this;
    }

    public ResourceEntity<T> excludeId() {
        this.idIncluded = false;
        return this;
    }

    public ResourceEntity<?> getMapBy() {
        return mapBy;
    }

    /**
     * @since 1.1
     */
    public ResourceEntity<T> mapBy(ResourceEntity<?> mapBy, String mapByPath) {
        this.mapByPath = mapByPath;
        this.mapBy = mapBy;
        return this;
    }

    public String getMapByPath() {
        return mapByPath;
    }

    @Override
    public String toString() {

        ToStringBuilder tsb = new ToStringBuilder(this);
        if (lrEntity != null) {
            tsb.append("name", lrEntity.getName());
        }

        return tsb.toString();
    }

    public Class<T> getType() {
        return lrEntity.getType();
    }

    /**
     * @since 1.20
     */
    public int getFetchOffset() {
        return fetchOffset;
    }

    /**
     * @since 1.20
     */
    public void setFetchOffset(int fetchOffset) {
        this.fetchOffset = fetchOffset;
    }

    /**
     * @since 1.20
     */
    public int getFetchLimit() {
        return fetchLimit;
    }

    /**
     * @since 1.20
     */
    public void setFetchLimit(int fetchLimit) {
        this.fetchLimit = fetchLimit;
    }

    /**
     * @since 1.20
     */
    public String getApplicationBase() {
        return applicationBase;
    }

    /**
     * @since 1.20
     */
    public void setApplicationBase(String applicationBase) {
        this.applicationBase = applicationBase;
    }

    /**
     * @since 1.23
     */
    public boolean isFiltered() {
        return filtered;
    }

    /**
     * @since 1.23
     */
    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }
}
