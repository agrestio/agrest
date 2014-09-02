package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectIdQuery;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.7
 */
abstract class KeyValueResponseObjectMapper<T> implements ResponseObjectMapper<T> {

	protected ObjectContext context;
	protected Class<T> type;
	protected ObjEntity entity;
	protected UpdateResponse<T> response;
	protected String keyPath;
	protected boolean supportsNullKey;

	protected KeyValueResponseObjectMapper(UpdateResponse<T> response, ObjectContext context, String keyPath,
			boolean supportsNullKey) {
		this.type = response.getType();
		this.entity = response.getEntity().getCayenneEntity();
		this.context = context;
		this.response = response;
		this.keyPath = keyPath;
		this.supportsNullKey = supportsNullKey;
	}

	protected abstract Object keyForUpdate(EntityUpdate u);

	protected abstract Object keyForObject(T object);

	@Override
	public boolean isIdempotent(EntityUpdate u) {
		return keyForUpdate(u) != null;
	}

	@Override
	public Object findParent() {
		return findById(response.getParent().getType(), response.getParent().getId());
	}

	@Override
	public Map<EntityUpdate, T> find() {

		if (response.getUpdates().isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Object, Collection<EntityUpdate>> updateMap = new HashMap<>();
		for (EntityUpdate u : response.getUpdates()) {
			mapUpdate(updateMap, u);
		}

		if (updateMap.isEmpty()) {
			return Collections.emptyMap();
		}

		List<T> objects = findObjects(updateMap);
		if (objects.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<EntityUpdate, T> objectMap = new HashMap<>((int) (objects.size() / 0.75));

		for (T o : objects) {
			mapObject(objectMap, updateMap, o);
		}

		return objectMap;
	}

	@Override
	public T create(EntityUpdate u) {

		T o = context.newObject(type);

		Object id = u.getId();

		// set explicit ID
		if (id != null) {

			if (response.isIdUpdatesDisallowed() && !u.isIdPropagated()) {
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

	protected List<T> findObjects(Map<Object, Collection<EntityUpdate>> updateMap) {

		// TODO: split query in batches

		// respect Constants.SERVER_MAX_ID_QUALIFIER_SIZE_PROPERTY
		// property of Cayenne , breaking the IN query into subqueries.
		// Otherwise this operation will not scale.. Though I guess since we are
		// not using streaming API to read data from Cayenne, we are already
		// limited in how much data can fit in the memory map.

		Collection<Object> values = updateMap.keySet();
		Expression qualifier = keyPath.startsWith("db:") ? ExpressionFactory.inDbExp(keyPath.substring(3), values)
				: ExpressionFactory.inExp(keyPath, values);

		SelectQuery<T> query = SelectQuery.query(type);
		query.setQualifier(qualifier);
		return context.select(query);
	}

	protected void mapUpdate(Map<Object, Collection<EntityUpdate>> map, EntityUpdate u) {

		Object key = keyForUpdate(u);

		if (key != null || supportsNullKey) {
			Collection<EntityUpdate> updatesForId = map.get(key);

			// potentially multiple updates per ID... store in collection
			if (updatesForId == null) {
				updatesForId = new ArrayList<>(2);
				map.put(key, updatesForId);
			}

			updatesForId.add(u);
		}
	}

	protected void mapObject(Map<EntityUpdate, T> objectMap, Map<Object, Collection<EntityUpdate>> updateMap, T o) {
		Object id = keyForObject(o);

		Collection<EntityUpdate> updatesForKey = updateMap.get(id);
		if (updatesForKey == null) {
			// completely unexpected...
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "No updates for id: " + id);
		}

		for (EntityUpdate u : updatesForKey) {
			objectMap.put(u, o);
		}
	}

}
