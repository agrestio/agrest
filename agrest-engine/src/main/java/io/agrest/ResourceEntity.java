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
 * A model of a resource entity for a given client request. ResourceEntity is based on an {@link AgEntity} with
 * request-specific changes. Connected ResourceEntities form a tree structure that overlays a certain subgraph of
 * AgEntities.
 */
public abstract class ResourceEntity<T> {

    private boolean idIncluded;

    private final AgEntity<T> agEntity;

    private final Map<String, AgAttribute> attributes;
    private final Set<String> defaultAttributes;
    private final Map<String, RelatedResourceEntity<?>> children;
    private final Map<String, Object> requestProperties;

    private String mapByPath;
    private ResourceEntity<?> mapBy;
    private final List<Sort> orderings;
    private Exp exp;
    private int start;
    private int limit;

    public ResourceEntity(AgEntity<T> agEntity) {

        this.agEntity = agEntity;

        this.idIncluded = false;
        this.attributes = new HashMap<>();
        this.defaultAttributes = new HashSet<>();
        this.children = new HashMap<>();
        this.orderings = new ArrayList<>(2);
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
     * @since 5.0
     */
    public Exp getExp() {
        return exp;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #getExp()}
     */
    @Deprecated
    public Exp getQualifier() {
        return getExp();
    }

    /**
     * @since 5.0
     */
    public void andExp(Exp exp) {
        this.exp = this.exp != null ? this.exp.and(exp) : exp;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #andExp(Exp)}
     */
    @Deprecated
    public void andQualifier(Exp qualifier) {
        andExp(qualifier);
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
    public RelatedResourceEntity<?> removeChild(String name) {
        return children.remove(name);
    }

    public Map<String, RelatedResourceEntity<?>> getChildren() {
        return children;
    }

    /**
     * @since 1.1
     */
    public RelatedResourceEntity<?> getChild(String name) {
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

    public Class<T> getType() {
        return agEntity.getType();
    }

    /**
     * @since 5.0
     */
    public int getStart() {
        return start;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #getStart()}
     */
    @Deprecated
    public int getFetchOffset() {
        return getStart();
    }

    /**
     * @since 5.0
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #setStart(int)}
     */
    @Deprecated
    public void setFetchOffset(int fetchOffset) {
        setStart(fetchOffset);
    }

    /**
     * @since 5.0
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #getLimit()}
     */
    @Deprecated
    public int getFetchLimit() {
        return getLimit();
    }

    /**
     * @since 5.0
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #setLimit(int)}
     */
    @Deprecated
    public void setFetchLimit(int fetchLimit) {
        setLimit(fetchLimit);
    }

    /**
     * @since 1.23
     */
    public boolean isFiltered() {
        return !agEntity.getReadFilter().allowsAll();
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
