package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

import javax.ws.rs.core.Response.Status;

/**
 * @since 1.16
 */
public class CayenneFullSyncStage<T extends DataObject> extends CayenneCreateOrUpdateStage<T> {

	public CayenneFullSyncStage(ProcessingStage<UpdateContext<T>, ? super T> next, IMetadataService metadataService,
								boolean idempotent) {
		super(next, metadataService, idempotent);
	}

	@Override
	protected void sync(UpdateContext<T> context) {

		ObjectMapper<T> mapper = createObjectMapper(context);
		Map<Object, Collection<EntityUpdate<T>>> keyMap = mutableKeyMap(context, mapper);

		List<T> allObjects = allItems(context);

		List<DataObject> deletedObjects = new ArrayList<>();

		for (T o : allObjects) {
			Object key = mapper.keyForObject(o);

			Collection<EntityUpdate<T>> updates = keyMap.remove(key);

			if (updates == null) {
				deletedObjects.add(o);
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

		// apply various request filters identifying the span of the collection

		if (context.getParent() != null) {
			EntityResolver resolver = CayenneContextInitStage.cayenneContext(context).getEntityResolver();
			query.andQualifier(context.getParent().qualifier(resolver));
		}

		if (context.getEntity().getQualifier() != null) {
			query.andQualifier(context.getEntity().getQualifier());
		}

		// TODO: use SelectBuilder to get Cayenne representation of the
		// resource, instead of duplicating this here...

		List<T> objects = CayenneContextInitStage.cayenneContext(context).select(query);
		if (context.isById() && objects.size() > 1) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, String.format(
					"Found more than one object for ID '%s' and entity '%s'",
					context.getId(), context.getEntity().getLrEntity().getName()));
		}
		return objects;
	}

}
