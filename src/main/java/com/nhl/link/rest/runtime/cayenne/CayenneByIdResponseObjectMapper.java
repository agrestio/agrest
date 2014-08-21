package com.nhl.link.rest.runtime.cayenne;

import java.util.Collection;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectIdQuery;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.4
 */
public class CayenneByIdResponseObjectMapper<T> implements ResponseObjectMapper<T> {

	private ObjectContext context;
	private Class<T> type;
	private ObjEntity entity;
	private UpdateResponse<T> response;

	public CayenneByIdResponseObjectMapper(UpdateResponse<T> response, ObjectContext context) {
		this.type = response.getType();
		this.entity = response.getEntity().getCayenneEntity();
		this.context = context;
		this.response = response;
	}

	@Override
	public boolean isIdempotent(EntityUpdate u) {
		return u.getId() != null;
	}

	@Override
	public Object findParent() {
		return findById(response.getParentType(), response.getParentId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public T find(EntityUpdate u) {
		if (u.getId() == null) {
			return null;
		}

		return (T) findById(entity, u.getId());
	}

	@Override
	public T create(EntityUpdate u) {

		T o = context.newObject(type);

		Object id = u.getId();

		// set explicit ID
		if (id != null) {

			if (response.isIdUpdatesDisallowed()) {
				throw new LinkRestException(Status.BAD_REQUEST, "Setting ID explicitly is not allowed: " + id);
			}

			// TODO: compile ID strategy to avoid recalculating metadata all the
			// time...

			Collection<DbAttribute> pks = entity.getDbEntity().getPrimaryKeys();
			if (pks.size() != 1) {
				throw new IllegalStateException(String.format("Unexpected PK size of %s for entity '%s'",
						entity.getName(), pks.size()));
			}

			DataObject dataObject = (DataObject) o;

			DbAttribute pk = pks.iterator().next();
			// reuse the ID...
			// 1. meaningful ID
			ObjAttribute opk = entity.getAttributeForDbAttribute(pk);
			if (opk != null) {
				dataObject.writeProperty(opk.getName(), id);
			}
			// 2. PK is propagated from the parent
			// else if () {}
			//
			// 3. PK is auto-generated ... I guess this is sorta
			// expected to fail - generated meaningless PK should not be
			// pushed from the client
			else if (pk.isGenerated()) {
				throw new LinkRestException(Status.BAD_REQUEST, "Can't create '" + entity.getName() + "' with fixed id");
			}
			// 4. just some ID desired by the client...
			else {
				// TODO: hopefully this works..
				dataObject.getObjectId().getReplacementIdMap().put(pk.getName(), id);
			}
		}

		return o;
	}

	@SuppressWarnings("unchecked")
	protected <A> A findById(Class<A> type, Object id) {
		ObjEntity entity = context.getEntityResolver().getObjEntity(type);

		return (A) findById(entity, id);
	}

	protected Object findById(ObjEntity entity, Object id) {

		// sanity checking...
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
		}

		if (id == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No id specified");
		}

		String idName = entity.getPrimaryKeyNames().iterator().next();
		ObjectIdQuery select = new ObjectIdQuery(new ObjectId(entity.getName(), idName, id));
		return Cayenne.objectForQuery(context, select);
	}

}
