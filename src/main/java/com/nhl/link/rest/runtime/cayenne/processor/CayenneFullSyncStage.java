package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public class CayenneFullSyncStage<T extends DataObject> extends CayenneCreateOrUpdateStage<T> {

	public CayenneFullSyncStage(ProcessingStage<UpdateContext<T>, ? super T> next, boolean idempotent) {
		super(next, idempotent);
	}

	@Override
	protected void sync(UpdateContext<T> context) {

		ObjectMapper<T> mapper = createObjectMapper(context);
		Map<Object, Collection<EntityUpdate>> keyMap = mutableKeyMap(context, mapper);

		List<T> allObjects = allItems(context);

		List<DataObject> deletedObjects = new ArrayList<>();

		for (T o : allObjects) {
			Object key = mapper.keyForObject(o);

			Collection<EntityUpdate> updates = keyMap.remove(key);

			if (updates == null) {
				deletedObjects.add((DataObject) o);
			} else {
				updateSingle(context, o, updates);
			}
		}

		if (!deletedObjects.isEmpty()) {
			CayenneContextInitStage.cayenneContext(context).deleteObjects(deletedObjects);
		}

		// check leftovers - those correspond to objects missing in the DB or
		// objects with no keys
		afterUpdatesMerge(context, keyMap);
	}

	List<T> allItems(UpdateContext<T> context) {
		SelectQuery<T> query = SelectQuery.query(context.getType());

		UpdateResponse<T> response = context.getResponse();

		// apply various request filters identifying the span of the collection

		if (response.getParent() != null) {
			EntityResolver resolver = CayenneContextInitStage.cayenneContext(context).getEntityResolver();
			query.andQualifier(response.getParent().qualifier(resolver));
		}

		if (response.getEntity().getQualifier() != null) {
			query.andQualifier(response.getEntity().getQualifier());
		}

		// TODO: use SelectBuilder to get Cayenne representation of the
		// resource,
		// instead of duplicating this here...

		return CayenneContextInitStage.cayenneContext(context).select(query);
	}

}
