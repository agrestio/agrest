package io.agrest;

import io.agrest.base.protocol.Sort;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import org.apache.cayenne.exp.Expression;

import java.util.*;

/**
 * A metadata object that describes a data structure of a given REST resource. Connected ResourceEntities form a
 * tree structure that usually overlays a certain Cayenne mapping subgraph (unless this is a non-persistent entity),
 * filtering and extending its properties to describe the data structure to be returned to the client.
 * <p>
 * ResourceEntity scope is a single request. It is usually created by Agrest based on request parameters and can be
 * optionally further customized by the application via custom stages.
 */
public abstract class ResourceEntity<T> {

    private boolean idIncluded;

    private final AgEntity<T> agEntity;
    private final AgEntityOverlay<T> agEntityOverlay;

    private final Map<String, AgAttribute> attributes;
    private final Set<String> defaultAttributes;
    private final Map<String, NestedResourceEntity<?>> children;

    private String mapByPath;
    private ResourceEntity<?> mapBy;
    private final List<Sort> orderings;
    private Expression qualifier;
    private int fetchOffset;
    private int fetchLimit;
    private final List<EntityEncoderFilter> entityEncoderFilters;

    private final Map<String, Object> requestProperties;

    public ResourceEntity(AgEntity<T> agEntity, AgEntityOverlay<T> agEntityOverlay) {

        this.agEntity = agEntity;
        this.agEntityOverlay = agEntityOverlay;

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
     * @since 3.4
     */
    public AgEntityOverlay<T> getAgEntityOverlay() {
        return agEntityOverlay;
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

    public List<Sort> getOrderings() {
        return orderings;
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
        return !entityEncoderFilters.isEmpty();
    }

    /**
     * @since 3.4
     */
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
