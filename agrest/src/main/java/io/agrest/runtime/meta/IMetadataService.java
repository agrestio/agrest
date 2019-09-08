package io.agrest.runtime.meta;

import io.agrest.AgException;
import io.agrest.EntityParent;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import javax.ws.rs.core.Response;
import java.lang.reflect.Type;

/**
 * Provides access to Agrest entity metadata.
 */
public interface IMetadataService {

    /**
     * @since 1.12
     */
    <T> AgEntity<T> getAgEntity(Class<T> type);

    /**
     * @since 3.4
     */
    <T> AgEntity<T> getAgEntityByType(Type type);

    /**
     * @deprecated since 3.4 renamed to {@link #getAgEntityByType(Type)}
     */
    @Deprecated
    default <T> AgEntity<T> getEntityByType(Type type) {
        return getAgEntityByType(type);
    }

    /**
     * Returns a named relationship for a given object type. If the type is not supported or there is no matching
     * relationship, an exception is thrown.
     *
     * @since 1.12
     */
    default AgRelationship getAgRelationship(Class<?> type, String relationship) {
        AgRelationship r = getAgEntity(type).getRelationship(relationship);

        if (r == null) {
            throw new AgException(Response.Status.BAD_REQUEST, "Invalid relationship: '" + relationship + "'");
        }

        return r;
    }

    /**
     * Returns a relationship to child for a given {@link EntityParent}. If the
     * type is not supported or there is no matching relationship, an exception
     * is thrown.
     *
     * @since 1.12
     * @deprecated since 3.4. Seems redundant and simply cluttering the interface. Use {@link #getAgRelationship(Class, String)}
     * instead.
     */
    @Deprecated
    default AgRelationship getAgRelationship(EntityParent<?> parent) {
        return getAgRelationship(parent.getType(), parent.getRelationship());
    }
}
