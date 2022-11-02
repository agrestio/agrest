package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A model of a resource entity for a given client request. ResourceEntity is based on an {@link AgEntity} with
 * request-specific changes. Connected ResourceEntities form a tree structure that overlays a certain subgraph of
 * AgEntities.
 */
public abstract class ResourceEntity<T> {

    private boolean idIncluded;

    private final ResourceEntityProjection<T> baseProjection;
    private final Map<Class<? extends T>, ResourceEntityProjection<? extends T>> projections;
    private final Map<String, RelatedResourceEntity<?>> children;
    private final Map<String, Object> properties;

    private ResourceEntity<?> mapBy;
    private final List<Sort> orderings;
    private Exp exp;
    private int start;
    private int limit;

    public ResourceEntity(AgEntity<T> agEntity) {

        List<ResourceEntityProjection<? extends T>> projections = buildProjections(agEntity, new ArrayList<>());
        this.baseProjection = (ResourceEntityProjection<T>) projections.get(0);
        this.projections = projectionsByType(projections);
        this.idIncluded = false;
        this.children = new HashMap<>();
        this.orderings = new ArrayList<>(2);
        this.properties = new HashMap<>(5);
    }

    private static <T> List<ResourceEntityProjection<? extends T>> buildProjections(
            AgEntity<? extends T> entity,
            List<ResourceEntityProjection<? extends T>> projections) {

        // generally we should not need a projection for abstract entities, but since there's still too much
        // code relying on "baseProjection", we don't check for "abstract" flag here, and create projections
        // for every entity

        projections.add(new ResourceEntityProjection<>(entity));
        entity.getSubEntities().forEach(se -> buildProjections(se, projections));
        return projections;
    }

    private static <T> Map<Class<? extends T>, ResourceEntityProjection<? extends T>> projectionsByType(
            List<ResourceEntityProjection<? extends T>> projections) {

        if (projections.size() == 1) {
            return Map.of(projections.get(0).getAgEntity().getType(), projections.get(0));
        }

        Map<Class<? extends T>, ResourceEntityProjection<? extends T>> byType = new HashMap<>();
        projections.forEach(p -> byType.put(p.getAgEntity().getType(), p));
        return byType;
    }

    /**
     * @since 3.4
     */
    public String getName() {
        return getAgEntity().getName();
    }

    public Class<T> getType() {
        return getAgEntity().getType();
    }

    /**
     * Returns the projection object associated with this ResourceEntity that corresponds to the topmost superclass
     * of the entity. I.e. the same type as the T parameter of the entity.
     *
     * @since 5.0
     */
    public ResourceEntityProjection<T> getBaseProjection() {
        return baseProjection;
    }

    /**
     * @since 5.0
     */
    public <S extends T> ResourceEntityProjection<S> getProjection(Class<S> type) {
        return (ResourceEntityProjection<S>) projections.get(type);
    }

    /**
     * @since 5.0
     */
    public Collection<ResourceEntityProjection<? extends T>> getProjections() {
        return projections.values();
    }

    /**
     * @since 1.12
     */
    public AgEntity<T> getAgEntity() {
        return getBaseProjection().getAgEntity();
    }

    /**
     * Returns a previously stored custom object for a given name. The properties mechanism allows pluggable processing
     * pipelines to store and exchange data within a given request.
     *
     * @since 5.0
     */
    public <P> P getProperty(String name) {
        return (P) properties.get(name);
    }

    /**
     * Sets a property value for a given name. The properties mechanism allows pluggable processing pipelines to
     * store and exchange data within a given request.
     *
     * @since 5.0
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * @since 5.0
     */
    public Exp getExp() {
        return exp;
    }

    /**
     * @deprecated in favor of {@link #getExp()}
     */
    @Deprecated(since = "5.0")
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
     * @deprecated in favor of {@link #andExp(Exp)}
     */
    @Deprecated(since = "5.0")
    public void andQualifier(Exp qualifier) {
        andExp(qualifier);
    }

    public List<Sort> getOrderings() {
        return orderings;
    }

    /**
     * @return true if the attribute was added to one of the underlying the projections or was already a part of one
     * of the projections
     * @since 5.0
     */
    public boolean ensureAttribute(String attributeName, boolean isDefault) {

        boolean success = false;
        for (ResourceEntityProjection<?> p : projections.values()) {
            success = p.ensureAttribute(attributeName, isDefault) || success;
        }
        return success;
    }

    /**
     * @since 5.0
     */
    public boolean removeAttribute(String attributeName) {

        boolean removed = false;
        for (ResourceEntityProjection<?> p : projections.values()) {
            removed = p.removeAttribute(attributeName) || removed;
        }
        return removed;
    }

    /**
     * @since 3.7
     */
    public boolean removeChild(String name) {

        boolean removed = false;
        for (ResourceEntityProjection<?> p : projections.values()) {
            removed = p.removeRelationship(name) || removed;
        }

        if (removed) {
            children.remove(name);
        }

        return removed;
    }

    /**
     * @since 5.0
     */
    public boolean ensureRelationship(String relationshipName) {
        boolean success = false;
        for (ResourceEntityProjection<?> p : projections.values()) {
            success = p.ensureRelationship(relationshipName) || success;
        }
        return success;
    }

    /**
     * @since 5.0
     */
    public RelatedResourceEntity<?> ensureChild(
            String relationshipName,
            BiFunction<ResourceEntity<?>, String, RelatedResourceEntity<?>> childCreator) {

        return children.computeIfAbsent(relationshipName, r -> childCreator.apply(this, r));
    }

    /**
     * @since 5.0
     */
    public Collection<RelatedResourceEntity<?>> getChildren() {
        return children.values();
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
     * @deprecated in favor of {@link #mapBy(ResourceEntity)}. mapByPath parameter is ignored.
     */
    @Deprecated(since = "5.0")
    public ResourceEntity<T> mapBy(ResourceEntity<?> mapBy, String mapByPath) {
        return mapBy(mapBy);
    }

    /**
     * @since 5.0
     */
    public ResourceEntity<T> mapBy(ResourceEntity<?> mapBy) {
        this.mapBy = mapBy;
        return this;
    }

    /**
     * @since 5.0
     */
    public int getStart() {
        return start;
    }

    /**
     * @deprecated in favor of {@link #getStart()}
     */
    @Deprecated(since = "5.0")
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
     * @deprecated in favor of {@link #setStart(int)}
     */
    @Deprecated(since = "5.0")
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
     * @deprecated in favor of {@link #getLimit()}
     */
    @Deprecated(since = "5.0")
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
     * @deprecated in favor of {@link #setLimit(int)}
     */
    @Deprecated(since = "5.0")
    public void setFetchLimit(int fetchLimit) {
        setLimit(fetchLimit);
    }

    /**
     * @since 1.23
     */
    public boolean isFiltered() {
        for (ResourceEntityProjection<?> p : projections.values()) {
            if (!p.getAgEntity().getReadFilter().allowsAll()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @deprecated in favor of {@link #getProperty(String)}
     */
    @Deprecated(since = "5.0")
    public <P> P getRequestProperty(String name) {
        return getProperty(name);
    }

    /**
     * @deprecated in favor of {@link #setProperty(String, Object)}
     */
    @Deprecated(since = "5.0")
    public void setRequestProperty(String name, Object value) {
        setProperty(name, value);
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
