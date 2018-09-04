package io.agrest.runtime.meta;

import io.agrest.AgRESTException;
import io.agrest.EntityParent;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import javax.ws.rs.core.Response;
import java.lang.reflect.Type;

/**
 * Provides access to AgREST entity metadata.
 */
public interface IMetadataService {

	/**
	 * @since 1.12
	 */
	<T> AgEntity<T> getLrEntity(Class<T> type);

	/**
	 * Returns a named relationship for a given object type. If the type is not
	 * supported or there is no matching relationship, an exception is thrown.
	 * 
	 * @since 1.12
	 */
	default AgRelationship getLrRelationship(Class<?> type, String relationship) {
		AgEntity<?> e = getLrEntity(type);
		AgRelationship r = e.getRelationship(relationship);
		if (r == null) {
			throw new AgRESTException(Response.Status.BAD_REQUEST, "Invalid relationship: '" + relationship + "'");
		}

		return r;
	}

	/**
	 * Returns a relationship to child for a given {@link EntityParent}. If the
	 * type is not supported or there is no matching relationship, an exception
	 * is thrown.
	 * 
	 * @since 1.12
	 */
	default AgRelationship getLrRelationship(EntityParent<?> parent) {
		return getLrRelationship(parent.getType(), parent.getRelationship());
	}

	/**
     * @since 2.3
     */
	<T> AgEntity<T> getEntityByType(Type entityType);
}
