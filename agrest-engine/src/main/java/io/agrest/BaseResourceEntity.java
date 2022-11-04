package io.agrest;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 5.0
 */
public abstract class BaseResourceEntity<T> implements ResourceEntity<T> {

    private boolean idIncluded;

    private final AgEntity<T> agEntity;

    private final Map<String, AgAttribute> attributes;
    private final Set<String> defaultAttributes;
    private final Map<String, RelatedResourceEntity<?>> children;
    private final Map<String, Object> properties;

    private ResourceEntity<?> mapBy;
    private final List<Sort> orderings;
    private Exp exp;
    private int start;
    private int limit;

    public BaseResourceEntity(AgEntity<T> agEntity) {

        this.agEntity = agEntity;

        this.idIncluded = false;
        this.attributes = new HashMap<>();
        this.defaultAttributes = new HashSet<>();
        this.children = new HashMap<>();
        this.orderings = new ArrayList<>(2);
        this.properties = new HashMap<>(5);
    }

    /**
     * @since 5.0
     */
    @Override
    public <ST extends T> ResourceEntity<ST> asSubEntity(AgEntity<ST> subEntity) {
        return this.agEntity == subEntity && this.agEntity.getSubEntities().isEmpty()
                ? (ResourceEntity<ST>) this
                : new FilteredResourceEntity(subEntity, this);
    }

    /**
     * @since 1.12
     */
    @Override
    public AgEntity<T> getAgEntity() {
        return agEntity;
    }

    /**
     * @since 5.0
     */
    @Override
    public Exp getExp() {
        return exp;
    }

    /**
     * @since 5.0
     */
    @Override
    public void andExp(Exp exp) {
        this.exp = this.exp != null ? this.exp.and(exp) : exp;
    }

    @Override
    public List<Sort> getOrderings() {
        return orderings;
    }

    /**
     * @since 4.7
     */
    @Override
    public AgAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * @since 1.12
     */
    @Override
    public Map<String, AgAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Returns whether the named attribute was added to the entity implicitly, via the default rules, instead of being
     * explicitly requested by the client.
     *
     * @since 3.7
     */
    @Override
    public boolean isDefaultAttribute(String name) {
        return defaultAttributes.contains(name);
    }

    /**
     * @since 3.7
     */
    @Override
    public void addAttribute(AgAttribute attribute, boolean isDefault) {
        attributes.put(attribute.getName(), attribute);
        if (isDefault) {
            defaultAttributes.add(attribute.getName());
        }
    }

    /**
     * @since 3.7
     */
    @Override
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
    @Override
    public RelatedResourceEntity<?> removeChild(String name) {
        return children.remove(name);
    }

    @Override
    public Map<String, RelatedResourceEntity<?>> getChildren() {
        return children;
    }

    /**
     * @since 1.1
     */
    @Override
    public RelatedResourceEntity<?> getChild(String name) {
        return children.get(name);
    }

    public boolean isIdIncluded() {
        return idIncluded;
    }

    @Override
    public ResourceEntity<T> includeId(boolean include) {
        this.idIncluded = include;
        return this;
    }

    @Override
    public ResourceEntity<T> includeId() {
        this.idIncluded = true;
        return this;
    }

    @Override
    public ResourceEntity<T> excludeId() {
        this.idIncluded = false;
        return this;
    }

    @Override
    public ResourceEntity<?> getMapBy() {
        return mapBy;
    }

    /**
     * @since 5.0
     */
    @Override
    public int getStart() {
        return start;
    }

    /**
     * @since 5.0
     */
    @Override
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @since 5.0
     */
    @Override
    public int getLimit() {
        return limit;
    }

    /**
     * @since 5.0
     */
    @Override
    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public ResourceEntity<T> mapBy(ResourceEntity<?> mapBy) {
        this.mapBy = mapBy;
        return this;
    }

    /**
     * @since 1.23
     */
    @Override
    public boolean isFiltered() {
        return !agEntity.getReadFilter().allowsAll();
    }

    /**
     * Returns a previously stored custom object for a given name. The properties mechanism allows pluggable processing
     * pipelines to store and exchange data within a given request.
     *
     * @since 5.0
     */
    @Override
    public <P> P getProperty(String name) {
        return (P) properties.get(name);
    }

    /**
     * Sets a property value for a given name. The properties mechanism allows pluggable processing pipelines to
     * store and exchange data within a given request.
     *
     * @since 5.0
     */
    @Override
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * Returns a sublist of the data collection with "start" and "limit" constraints applied if present.
     *
     * @since 5.0
     */
    protected List<T> getDataWindow(List<T> dataUnlimited) {

        // not all resolvers
        if (dataUnlimited == null) {
            return null;
        }

        int total = dataUnlimited.size();

        if (total == 0 || (start <= 0 && limit <= 0)) {
            return dataUnlimited;
        }

        int i0 = Math.max(start, 0);
        if (i0 >= total) {
            return Collections.emptyList();
        }

        int i1 = limit > 0 ? Math.min(i0 + limit, total) : total;

        return dataUnlimited.subList(i0, i1);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + getName() + ']';
    }
}
