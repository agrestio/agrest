package io.agrest;

import io.agrest.base.protocol.Exp;
import io.agrest.base.protocol.Sort;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A model of a resource entity for a given client request. ResourceEntity is based on an {@link AgEntity} with
 * request-specific changes. Connected ResourceEntities form a tree structure that overlays a certain subgraph of
 * AgEntities.
 */
public abstract class ResourceEntity<T> {

    private boolean idIncluded;

    private final AgEntity<T> agEntity;

    private final Map<String, AgAttribute> attributes;
    private final Set<String> defaultAttributes;
    private final Map<String, NestedResourceEntity<?>> children;

    private String mapByPath;
    private ResourceEntity<?> mapBy;
    private final List<Sort> orderings;
    private Exp qualifier;
    private int fetchOffset;
    private int fetchLimit;
    private final List<EntityEncoderFilter> entityEncoderFilters;

    private final Map<String, Object> requestProperties;

    public ResourceEntity(AgEntity<T> agEntity) {

        this.agEntity = agEntity;

        this.idIncluded = false;
        this.attributes = new HashMap<>();
        this.defaultAttributes = new HashSet<>();
        this.children = new HashMap<>();
        this.orderings = new ArrayList<>(2);
        this.entityEncoderFilters = new ArrayList<>(3);

        this.requestProperties = new HashMap<>(5);
    }

    /**
     * @since 3.4
     */
    public String getName() {
        return agEntity.getName();
    }

    /**
     * @since 1.12
     */
    public AgEntity<T> getAgEntity() {
        return agEntity;
    }

    /**
     * @since 4.4
     */
    public Exp getQualifier() {
        return qualifier;
    }

    /**
     * @since 4.4
     */
    public void andQualifier(Exp qualifier) {
        this.qualifier = this.qualifier != null ? this.qualifier.and(qualifier) : qualifier;
    }

    public List<Sort> getOrderings() {
        return orderings;
    }

    /**
     * @since 4.7
     */
    public AgAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * @since 1.12
     */
    public Map<String, AgAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Returns whether the named attribute was added to the entity implicitly, via the default rules, instead of being
     * explicitly requested by the client.
     *
     * @since 3.7
     */
    public boolean isDefaultAttribute(String name) {
        return defaultAttributes.contains(name);
    }

    /**
     * @since 3.7
     */
    public void addAttribute(AgAttribute attribute, boolean isDefault) {
        attributes.put(attribute.getName(), attribute);
        if (isDefault) {
            defaultAttributes.add(attribute.getName());
        }
    }

    /**
     * @since 3.7
     */
    public AgAttribute removeAttribute(String name) {

        AgAttribute removed = attributes.remove(name);
        if (removed != null) {
            defaultAttributes.remove(name);
        }

        return removed;
    }

    /**
     * @since 3.7
     */
    public NestedResourceEntity<?> removeChild(String name) {
        return children.remove(name);
    }

    public Map<String, NestedResourceEntity<?>> getChildren() {
        return children;
    }

    /**
     * @since 1.1
     */
    public NestedResourceEntity<?> getChild(String name) {
        return children.get(name);
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
        return "ResourceEntity{" +
                "name=" + agEntity.getName() +
                '}';
    }

    public Class<T> getType() {
        return agEntity.getType();
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
     * @since 1.23
     */
    public boolean isFiltered() {
        return !(entityEncoderFilters.isEmpty() && agEntity.getReadableObjectFilter().allowsAll());
    }

    /**
     * @since 3.4
     * @deprecated since 4.8 in favor of {@link io.agrest.filter.ObjectFilter}.
     */
    @Deprecated
    public List<EntityEncoderFilter> getEntityEncoderFilters() {
        return entityEncoderFilters;
    }

    /**
     * Returns a previously stored object for a given name. Request properties mechanism allows pluggable processing
     * pipelines to store and exchange data within a given request.
     *
     * @since 3.7
     */
    public <P> P getRequestProperty(String name) {
        return (P) requestProperties.get(name);
    }

    /**
     * Sets a property value for a given name. Request properties mechanism allows pluggable processing pipelines to
     * store and exchange data within a given request.
     *
     * @since 3.7
     */
    public void setRequestProperty(String name, Object value) {
        requestProperties.put(name, value);
    }
}
