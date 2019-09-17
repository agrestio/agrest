package io.agrest;

import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.util.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

    private AgEntity<T> agEntity;
    private AgEntityOverlay<T> agEntityOverlay;
    private Map<String, AgAttribute> attributes;
    private Collection<String> defaultProperties;

    private String mapByPath;
    private ResourceEntity<?> mapBy;
    private Map<String, ChildResourceEntity<?>> children;
    private List<Ordering> orderings;
    private Expression qualifier;
    private Map<String, EntityProperty> includedExtraProperties;
    private Map<String, EntityProperty> extraProperties;
    private int fetchOffset;
    private int fetchLimit;
    private List<EntityEncoderFilter> entityEncoderFilters;

    private SelectQuery<T> select;

    public ResourceEntity(AgEntity<T> agEntity, AgEntityOverlay<T> agEntityOverlay) {

        this.agEntity = agEntity;
        this.agEntityOverlay = agEntityOverlay;

        this.idIncluded = false;
        this.attributes = new HashMap<>();
        this.defaultProperties = new HashSet<>();
        this.children = new HashMap<>();
        this.orderings = new ArrayList<>(2);
        this.extraProperties = new HashMap<>();
        this.includedExtraProperties = new HashMap<>();
        this.entityEncoderFilters = new ArrayList<>(3);
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

    public List<Ordering> getOrderings() {
        return orderings;
    }

    public SelectQuery<T> getSelect() {
        return select;
    }

    public void setSelect(SelectQuery<T> select) {
        this.select = select;
    }


    /**
     * @since 1.12
     */
    public Map<String, AgAttribute> getAttributes() {
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

    public Map<String, ChildResourceEntity<?>> getChildren() {
        return children;
    }

    /**
     * @since 1.1
     */
    public ChildResourceEntity<?> getChild(String name) {
        return children.get(name);
    }

    public Map<String, EntityProperty> getExtraProperties() {
        return extraProperties;
    }

    public Map<String, EntityProperty> getIncludedExtraProperties() {
        return includedExtraProperties;
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
        if (agEntity != null) {
            tsb.append("name", agEntity.getName());
        }

        return tsb.toString();
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
}
