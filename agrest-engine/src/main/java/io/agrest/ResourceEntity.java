package io.agrest;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Sort;

import java.util.List;
import java.util.Map;

/**
 * A model of a resource entity for a given client request. ResourceEntity is based on an {@link AgEntity} with
 * request-specific changes. Connected ResourceEntities form a tree structure that overlays a certain subgraph of
 * AgEntities.
 */
public interface ResourceEntity<T> {

    /**
     * @since 3.4
     */
    default String getName() {
        return getAgEntity().getName();
    }

    default Class<T> getType() {
        return getAgEntity().getType();
    }

    /**
     * @since 1.12
     */
    AgEntity<T> getAgEntity();

    /**
     * Returns a previously stored custom object for a given name. The properties mechanism allows pluggable processing
     * pipelines to store and exchange data within a given request.
     *
     * @since 5.0
     */
    <P> P getProperty(String name);

    /**
     * Sets a property value for a given name. The properties mechanism allows pluggable processing pipelines to
     * store and exchange data within a given request.
     *
     * @since 5.0
     */
    void setProperty(String name, Object value);

    /**
     * Returns a read-only "view" of this ResourceEntity with attributes and relationships filtered for a particular
     * subclass from the entity type inheritance hierarchy.
     *
     * @since 5.0
     */
    <ST extends T> ResourceEntity<ST> asSubEntity(AgEntity<ST> subEntity);

    /**
     * @since 5.0
     */
    Exp getExp();

    /**
     * @deprecated in favor of {@link #getExp()}
     */
    @Deprecated(since = "5.0")
    default Exp getQualifier() {
        return getExp();
    }

    /**
     * @since 5.0
     */
    void andExp(Exp exp);

    /**
     * @deprecated in favor of {@link #andExp(Exp)}
     */
    @Deprecated(since = "5.0")
    default void andQualifier(Exp qualifier) {
        andExp(qualifier);
    }

    List<Sort> getOrderings();

    /**
     * @since 4.7
     */
    AgAttribute getAttribute(String name);

    /**
     * @since 1.12
     */
    Map<String, AgAttribute> getAttributes();

    /**
     * Returns whether the named attribute was added to the entity implicitly, via the default rules, instead of being
     * explicitly requested by the client.
     *
     * @since 3.7
     */
    boolean isDefaultAttribute(String name);

    /**
     * @since 3.7
     */
    void addAttribute(AgAttribute attribute, boolean isDefault);

    /**
     * @since 3.7
     */
    AgAttribute removeAttribute(String name);

    /**
     * @since 3.7
     */
    RelatedResourceEntity<?> removeChild(String name);

    Map<String, RelatedResourceEntity<?>> getChildren();

    /**
     * @since 1.1
     */
    RelatedResourceEntity<?> getChild(String name);

    boolean isIdIncluded();

    ResourceEntity<T> includeId(boolean include);

    ResourceEntity<T> includeId();

    ResourceEntity<T> excludeId();

    ResourceEntity<?> getMapBy();

    /**
     * @since 1.1
     * @deprecated in favor of {@link #mapBy(ResourceEntity)}. mapByPath parameter is ignored.
     */
    @Deprecated(since = "5.0")
    default ResourceEntity<T> mapBy(ResourceEntity<?> mapBy, String mapByPath) {
        return mapBy(mapBy);
    }

    /**
     * @since 5.0
     */
    ResourceEntity<T> mapBy(ResourceEntity<?> mapBy);

    /**
     * @since 5.0
     */
    int getStart();

    /**
     * @deprecated in favor of {@link #getStart()}
     */
    @Deprecated(since = "5.0")
    default int getFetchOffset() {
        return getStart();
    }

    /**
     * @since 5.0
     */
    void setStart(int start);

    /**
     * @deprecated in favor of {@link #setStart(int)}
     */
    @Deprecated(since = "5.0")
    default void setFetchOffset(int fetchOffset) {
        setStart(fetchOffset);
    }

    /**
     * @since 5.0
     */
    int getLimit();

    /**
     * @deprecated in favor of {@link #getLimit()}
     */
    @Deprecated(since = "5.0")
    default int getFetchLimit() {
        return getLimit();
    }

    /**
     * @since 5.0
     */
    void setLimit(int limit);

    /**
     * @deprecated in favor of {@link #setLimit(int)}
     */
    @Deprecated(since = "5.0")
    default void setFetchLimit(int fetchLimit) {
        setLimit(fetchLimit);
    }

    /**
     * @since 1.23
     */
    boolean isFiltered();

    /**
     * @deprecated in favor of {@link #getProperty(String)}
     */
    @Deprecated(since = "5.0")
    default <P> P getRequestProperty(String name) {
        return getProperty(name);
    }

    /**
     * @deprecated in favor of {@link #setProperty(String, Object)}
     */
    @Deprecated(since = "5.0")
    default void setRequestProperty(String name, Object value) {
        setProperty(name, value);
    }
}
