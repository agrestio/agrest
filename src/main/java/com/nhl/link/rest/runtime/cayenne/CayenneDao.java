package com.nhl.link.rest.runtime.cayenne;

import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.ObjectIdQuery;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.dao.EntityDao;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class CayenneDao<T> implements EntityDao<T> {

	private Class<T> type;
	private ICayennePersister cayenneService;
	private IEncoderService encoderService;
	private IRequestParser requestParser;

	public CayenneDao(Class<T> type, IRequestParser requestParser, IEncoderService encoderService,
			ICayennePersister cayenneService) {
		this.type = type;
		this.requestParser = requestParser;
		this.encoderService = encoderService;
		this.cayenneService = cayenneService;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public SelectBuilder<T> forSelect() {
		return new CayenneSelectBuilder<T>(type, cayenneService, encoderService, requestParser);
	}

	@Override
	public SelectBuilder<T> forSelect(SelectQuery<T> query) {
		return new CayenneSelectBuilder<T>(query, type, cayenneService, encoderService, requestParser);
	}

	@Override
	public T insert(UpdateResponse<T> response) {
		ObjectContext context = cayenneService.newContext();
		T object = context.newObject(type);

		mergeChanges(response, object);
		context.commitChanges();
		return object;
	}

	@Override
	public T update(UpdateResponse<T> response) {
		ObjectContext context = cayenneService.newContext();
		T object = getExistingObject(type, context, response.getId());

		mergeChanges(response, object);
		context.commitChanges();
		return object;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete(Object id) {
		ObjectContext context = cayenneService.newContext();
		T object = getExistingObject(type, context, id);
		context.deleteObjects(object);
		context.commitChanges();
	}

	private void mergeChanges(UpdateResponse<T> response, T object) {

		DataObject dataObject = (DataObject) object;

		// attributes
		for (Entry<String, Object> e : response.getValues().entrySet()) {
			dataObject.writeProperty(e.getKey(), e.getValue());
		}

		// to-one relationships
		if (!response.getRelatedIds().isEmpty()) {

			ObjEntity rootEntity = response.getEntity().getEntity();
			ObjectContext context = dataObject.getObjectContext();

			for (Entry<String, Object> e : response.getRelatedIds().entrySet()) {

				if (e.getValue() == null) {
					dataObject.setToOneTarget(e.getKey(), null, true);
					continue;
				}

				ObjRelationship relationship = (ObjRelationship) rootEntity.getRelationship(e.getKey());

				ClassDescriptor relatedDescriptor = context.getEntityResolver().getClassDescriptor(
						relationship.getTargetEntityName());
				DataObject related = (DataObject) Cayenne.objectForPK(context, relatedDescriptor.getObjectClass(),
						e.getValue());

				if (related == null) {
					throw new LinkRestException(Status.NOT_FOUND, "Related object '"
							+ relationship.getTargetEntityName() + "' with ID '" + e.getValue() + "' is not found");
				}

				dataObject.setToOneTarget(e.getKey(), related, true);
			}
		}
	}

	private T getExistingObject(Class<T> type, ObjectContext context, Object id) {

		ObjEntity entity = context.getEntityResolver().getObjEntity(type);

		// sanity checking...
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
		}

		// TODO: should we start using optimistic locking on PK by default
		// instead of SELECT/DELETE|UPDATE?

		String idName = entity.getPrimaryKeyNames().iterator().next();
		ObjectIdQuery select = new ObjectIdQuery(new ObjectId(entity.getName(), idName, id));

		@SuppressWarnings("unchecked")
		T object = (T) Cayenne.objectForQuery(context, select);
		if (object == null) {
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
					+ entity.getName() + "'");
		}

		return object;
	}

}
