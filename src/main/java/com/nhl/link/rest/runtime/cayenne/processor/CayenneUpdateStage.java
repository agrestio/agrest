package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.cayenne.ByIdObjectMapperFactory;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public class CayenneUpdateStage<T extends DataObject> extends BaseCayenneUpdateStage<T> {

	public CayenneUpdateStage(ProcessingStage<UpdateContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void sync(UpdateContext<T> context) {

		ObjectMapper<T> mapper = createObjectMapper(context);

		Map<Object, Collection<EntityUpdate<T>>> keyMap = mutableKeyMap(context, mapper);

		for (T o : itemsForKeys(context, keyMap.keySet(), mapper)) {
			Object key = mapper.keyForObject(o);

			Collection<EntityUpdate<T>> updates = keyMap.remove(key);

			// a null can only mean some algorithm malfunction
			if (updates == null) {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Invalid key item: " + key);
			}

			updateSingle(context, o, updates);
		}

		// check leftovers - those correspond to objects missing in the DB or
		// objects with no keys
		afterUpdatesMerge(context, keyMap);
	}

	protected void afterUpdatesMerge(UpdateContext<T> context, Map<Object, Collection<EntityUpdate<T>>> keyMap) {
		if (!keyMap.isEmpty()) {
			Object firstKey = keyMap.keySet().iterator().next();

			if (firstKey == null) {
				throw new LinkRestException(Status.BAD_REQUEST, "Can't update. No id for object");
			}

			LrEntity<?> entity = context.getResponse().getEntity().getLrEntity();
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + firstKey + "' and entity '"
					+ entity.getName() + "'");
		}
	}

	protected Map<Object, Collection<EntityUpdate<T>>> mutableKeyMap(UpdateContext<T> context, ObjectMapper<T> mapper) {

		Collection<EntityUpdate<T>> updates = context.getUpdates();

		// sizing the map with one-update per key assumption
		Map<Object, Collection<EntityUpdate<T>>> map = new HashMap<>((int) (updates.size() / 0.75));

		for (EntityUpdate<T> u : updates) {

			Object key = mapper.keyForUpdate(u);
			Collection<EntityUpdate<T>> updatesForKey = map.get(key);
			if (updatesForKey == null) {
				updatesForKey = new ArrayList<>(2);
				map.put(key, updatesForKey);
			}

			updatesForKey.add(u);
		}

		return map;
	}

	protected ObjectMapper<T> createObjectMapper(UpdateContext<T> context) {
		ObjectMapperFactory mapper = context.getMapper() != null ? context.getMapper() : ByIdObjectMapperFactory
				.mapper();
		return mapper.forResponse(context.getResponse());
	}

	List<T> itemsForKeys(UpdateContext<T> context, Collection<Object> keys, ObjectMapper<T> mapper) {

		// TODO: split query in batches:
		// respect Constants.SERVER_MAX_ID_QUALIFIER_SIZE_PROPERTY
		// property of Cayenne , breaking query into subqueries.
		// Otherwise this operation will not scale.. Though I guess since we are
		// not using streaming API to read data from Cayenne, we are already
		// limited in how much data can fit in the memory map.

		List<Expression> expressions = new ArrayList<>(keys.size());
		for (Object key : keys) {

			Expression e = mapper.expressionForKey(key);
			if (e != null) {
				expressions.add(e);
			}
		}

		// no keys or all keys were for non-persistent objects
		if (expressions.isEmpty()) {
			return Collections.emptyList();
		}

		SelectQuery<T> query = SelectQuery.query(context.getType());
		query.setQualifier(ExpressionFactory.joinExp(Expression.OR, expressions));
		return CayenneContextInitStage.cayenneContext(context).select(query);
	}
}
