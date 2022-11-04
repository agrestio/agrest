package io.agrest.meta;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.reader.DataReader;
import io.agrest.resolver.RootDataResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A shared immutable model of a resource entity.
 *
 * @since 1.12
 */
public interface AgEntity<T> {

    /**
     * @return a mutable overlay object that can be used to customize the entity.
     * @since 3.4
     */
    static <T> AgEntityOverlay<T> overlay(Class<T> type) {
        return new AgEntityOverlay<>(type);
    }

    String getName();

    Class<T> getType();

    /**
     * @since 5.0
     */
    boolean isAbstract();

    /**
     * Returns a collection of direct sub-entities. I.e. the entities mapping subclasses of this entity's type.
     *
     * @since 5.0
     */
    Collection<AgEntity<? extends T>> getSubEntities();

    /**
     * @since 4.1
     */
    Collection<AgIdPart> getIdParts();

    /**
     * @since 4.1
     */
    AgIdPart getIdPart(String name);

    /**
     * Returns attributes that are either declared by this entity or are inherited from superclass.
     */
    Collection<AgAttribute> getAttributes();

    AgAttribute getAttribute(String name);

    /**
     * Returns attributes that are either declared by this entity, inherited from superclass, or declared in any
     * subclasses.
     *
     * @since 5.0
     */
    Collection<AgAttribute> getAttributesInHierarchy();

    /**
     * Returns a named attribute that is either declared by this entity, inherited from superclass, or declared in any
     * subclasses.
     *
     * @since 5.0
     */
    AgAttribute getAttributeInHierarchy(String name);

    /**
     * Returns relationships that are either declared by this entity or are inherited from superclass.
     */
    Collection<AgRelationship> getRelationships();

    AgRelationship getRelationship(String name);

    /**
     * Returns a named relationship that is either declared by this entity, inherited from superclass, or declared in any
     * subclasses.
     *
     * @since 5.0
     */
    AgRelationship getRelationshipInHierarchy(String name);

    /**
     * Returns relationships that are either declared by this entity, inherited from superclass, or declared in any
     * subclasses.
     *
     * @since 5.0
     */
    Collection<AgRelationship> getRelationshipsInHierarchy();

    /**
     * @return a default data resolver for this entity for when it is resolved as a root of a request.
     * @since 3.4
     */
    RootDataResolver<T> getDataResolver();

    /**
     * Returns a reader that allows to read all parts of the entity id at once as a Map.
     *
     * @since 4.2
     */
    default DataReader getIdReader() {
        Collection<AgIdPart> ids = getIdParts();
        switch (ids.size()) {
            case 0:
                throw new IllegalStateException("Can't create ID reader. No id parts defined for entity '" + getName() + "'");
            case 1:
                AgIdPart idPart = ids.iterator().next();
                return o -> Collections.singletonMap(idPart.getName(), idPart.getDataReader().read(o));
            default:
                return o -> {
                    Map<String, Object> values = new HashMap<>();
                    ids.forEach(idx -> values.put(idx.getName(), idx.getDataReader().read(o)));
                    return values;
                };
        }
    }

    /**
     * Returns an in-memory filter that will be applied to result objects during either "select" or one of the
     * "create" / "update" operations.
     *
     * @since 4.8
     */
    ReadFilter<T> getReadFilter();

    /**
     * Returns a predicate-like object applied to individual object CREATE operations.
     *
     * @since 4.8
     */
    CreateAuthorizer<T> getCreateAuthorizer();

    /**
     * Returns a predicate-like object applied to individual object UPDATE operations.
     *
     * @since 4.8
     */
    UpdateAuthorizer<T> getUpdateAuthorizer();

    /**
     * Returns a predicate-like object applied to individual object DELETE operations.
     *
     * @since 4.8
     */
    DeleteAuthorizer<T> getDeleteAuthorizer();

    /**
     * @since 5.0
     */
    default AgEntity<T> resolveOverlay(AgSchema schema, AgEntityOverlay<T> overlay) {
        return overlay != null && !overlay.isEmpty() ? overlay.resolve(schema, this, this.getSubEntities()) : this;
    }

    /**
     * @since 5.0
     */
    AgEntity<T> resolveOverlayHierarchy(AgSchema schema, Map<Class<?>, AgEntityOverlay<?>> overlays);
}
