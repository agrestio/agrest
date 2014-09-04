package com.nhl.link.rest.runtime.cayenne;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectIdQuery;

import com.nhl.link.rest.LinkRestException;

/**
 * @since 1.7
 */
class Util {

	private Util() {
	}

	/**
	 * @since 1.7
	 */
	@SuppressWarnings("unchecked")
	static <A> A findById(ObjectContext context, Class<A> type, Object id) {
		ObjEntity entity = context.getEntityResolver().getObjEntity(type);

		// sanity checking...
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
		}

		if (id == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No id specified");
		}

		String idName = entity.getPrimaryKeyNames().iterator().next();
		ObjectIdQuery select = new ObjectIdQuery(new ObjectId(entity.getName(), idName, id));
		return (A) Cayenne.objectForQuery(context, select);
	}
}
