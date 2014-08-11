package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.ObjectIdQuery;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.config.IConfigMerger;
import com.nhl.link.rest.runtime.dao.EntityDao;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class CayenneDao<T> implements EntityDao<T> {

	private Class<T> type;
	private ICayennePersister cayenneService;
	private IEncoderService encoderService;
	private IRequestParser requestParser;
	private IConfigMerger configMerger;
	private IMetadataService metadataService;

	public CayenneDao(Class<T> type, IRequestParser requestParser, IEncoderService encoderService,
			ICayennePersister cayenneService, IConfigMerger configMerger, IMetadataService metadataService) {
		this.type = type;
		this.requestParser = requestParser;
		this.encoderService = encoderService;
		this.cayenneService = cayenneService;
		this.configMerger = configMerger;
		this.metadataService = metadataService;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public SelectBuilder<T> forSelect() {
		return new CayenneSelectBuilder<T>(type, cayenneService, encoderService, requestParser, configMerger,
				metadataService);
	}

	@Override
	public SelectBuilder<T> forSelect(SelectQuery<T> query) {
		return new CayenneSelectBuilder<T>(query, type, cayenneService, encoderService, requestParser, configMerger,
				metadataService);
	}

	@Override
	public T insert(UpdateResponse<T> response) {
		ObjectContext context = cayenneService.newContext();
		T object = context.newObject(type);

		mergeChanges(response, (DataObject) object);
		context.commitChanges();
		return object;
	}

	@Override
	public T update(UpdateResponse<T> response) {
		ObjectContext context = cayenneService.newContext();
		T object = getExistingObject(type, context, response.getId());

		mergeChanges(response, (DataObject) object);
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

	private void mergeChanges(UpdateResponse<?> response, DataObject object) {

		// attributes
		for (Entry<String, Object> e : response.getValues().entrySet()) {
			object.writeProperty(e.getKey(), e.getValue());
		}

		// to-one relationships
		if (!response.getRelatedIds().isEmpty()) {

			ObjEntity rootEntity = response.getEntity().getCayenneEntity();
			ObjectContext context = object.getObjectContext();

			for (Entry<String, Object> e : response.getRelatedIds().entrySet()) {

				if (e.getValue() == null) {
					object.setToOneTarget(e.getKey(), null, true);
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

				object.setToOneTarget(e.getKey(), related, true);
			}
		}
	}

	private <A> A getExistingObject(Class<A> type, ObjectContext context, Object id) {

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
		A object = (A) Cayenne.objectForQuery(context, select);
		if (object == null) {
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
					+ entity.getName() + "'");
		}

		return object;
	}

	@Override
	public void unrelate(Object sourceId, String relationship) {

		// validate relationship before doing anything else
		ObjRelationship objRelationship = metadataService.getObjRelationship(type, relationship);

		ObjectContext context = cayenneService.newContext();
		DataObject src = (DataObject) getExistingObject(getType(), context, sourceId);

		boolean deleteTarget = mustDeleteTarget(objRelationship);

		if (objRelationship.isToMany()) {

			// clone relationship before we start deleting to avoid concurrent
			// modification of the iterator, and to be able to batch-delete
			// objects if needed
			@SuppressWarnings("unchecked")
			Collection<DataObject> relatedCollection = new ArrayList<>(
					(Collection<DataObject>) src.readProperty(relationship));

			for (DataObject o : relatedCollection) {
				src.removeToManyTarget(relationship, o, true);
			}

			if (deleteTarget) {
				context.deleteObjects(relatedCollection);
			}

		} else {

			DataObject target = (DataObject) src.readProperty(relationship);
			if (target != null) {
				src.setToOneTarget(relationship, null, true);

				if (deleteTarget) {
					context.deleteObjects(target);
				}
			}
		}

		context.commitChanges();
	}

	@Override
	public void unrelate(Object sourceId, String relationship, Object targetId) {

		// validate relationship before doing anything else
		ObjRelationship objRelationship = metadataService.getObjRelationship(type, relationship);

		ObjectContext context = cayenneService.newContext();

		DataObject src = (DataObject) getExistingObject(getType(), context, sourceId);

		boolean deleteTarget = mustDeleteTarget(objRelationship);
		Class<?> targetType = metadataService.getType(objRelationship.getTargetEntityName());

		// among other things this call checks that the target exists
		DataObject target = (DataObject) getExistingObject(targetType, context, targetId);

		if (objRelationship.isToMany()) {

			// sanity check...
			Collection<?> relatedCollection = (Collection<?>) src.readProperty(relationship);
			if (!relatedCollection.contains(target)) {
				throw new LinkRestException(Status.EXPECTATION_FAILED, "Source and target are not related");
			}

			src.removeToManyTarget(relationship, target, true);
		} else {

			// sanity check...
			if (src.readProperty(relationship) != target) {
				throw new LinkRestException(Status.EXPECTATION_FAILED, "Source and target are not related");
			}

			src.setToOneTarget(relationship, null, true);
		}

		if (deleteTarget) {
			context.deleteObjects(target);
		}

		context.commitChanges();
	}

	protected boolean mustDeleteTarget(ObjRelationship relationship) {

		// a rather vague algorithm for determining when a target can't exist
		// without source and should be deleted when relationship is broken

		if (relationship.isToMany() || relationship.isFlattened()) {
			return false;
		}

		DbRelationship dbRelationship = relationship.getDbRelationships().get(0);
		return dbRelationship.isToDependentPK();
	}

	// TODO: return root object with newly created related object?
	@Override
	public void relateNew(Object sourceId, String relationship, UpdateResponse<?> newTargetData) {

		// validate relationship before doing anything else
		ObjRelationship objRelationship = metadataService.getObjRelationship(type, relationship);

		// make sure source object exists before creating target
		ObjectContext context = cayenneService.newContext();
		DataObject src = (DataObject) getExistingObject(getType(), context, sourceId);

		Class<?> targetType = metadataService.getType(objRelationship.getTargetEntityName());

		DataObject target = (DataObject) context.newObject(targetType);
		mergeChanges(newTargetData, target);

		if (objRelationship.isToMany()) {
			src.addToManyTarget(relationship, target, true);
		} else {
			src.setToOneTarget(relationship, target, true);
		}

		context.commitChanges();
	}

	@Override
	public void relate(Object sourceId, String relationship, Object targetId) {
		// validate relationship before doing anything else
		ObjRelationship objRelationship = metadataService.getObjRelationship(type, relationship);

		// make sure source object exists before creating target
		ObjectContext context = cayenneService.newContext();
		DataObject src = (DataObject) getExistingObject(getType(), context, sourceId);

		Class<?> targetType = metadataService.getType(objRelationship.getTargetEntityName());
		DataObject target = (DataObject) getExistingObject(targetType, context, targetId);

		if (objRelationship.isToMany()) {
			src.addToManyTarget(relationship, target, true);
		} else {
			src.setToOneTarget(relationship, target, true);
		}

		context.commitChanges();
	}
}
