package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import org.apache.cayenne.query.Select;

import javax.ws.rs.core.Response;
import java.lang.reflect.Type;

/**
 * Provides access to LinkRest entity metadata.
 */
public interface IMetadataService {

	/**
	 * @since 1.12
	 */
	<T> LrEntity<T> getLrEntity(Class<T> type);

	/**
	 * @since 1.12
     * @deprecated since 2.5 unused and unneeded.
	 */
	@Deprecated
	<T> LrEntity<T> getLrEntity(Select<T> query);

	/**
	 * Returns a named relationship for a given object type. If the type is not
	 * supported or there is no matching relationship, an exception is thrown.
	 * 
	 * @since 1.12
	 */
	default LrRelationship getLrRelationship(Class<?> type, String relationship) {
		LrEntity<?> e = getLrEntity(type);
		LrRelationship r = e.getRelationship(relationship);
		if (r == null) {
			throw new LinkRestException(Response.Status.BAD_REQUEST, "Invalid relationship: '" + relationship + "'");
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
	default LrRelationship getLrRelationship(EntityParent<?> parent) {
		return getLrRelationship(parent.getType(), parent.getRelationship());
	}

	/**
     * @since 2.3
     */
	<T> LrEntity<T> getEntityByType(Type entityType);
}
