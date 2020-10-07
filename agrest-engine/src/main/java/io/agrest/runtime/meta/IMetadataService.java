package io.agrest.runtime.meta;

import io.agrest.AgException;
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
}
