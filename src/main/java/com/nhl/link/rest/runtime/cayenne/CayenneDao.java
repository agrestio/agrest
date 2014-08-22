package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;

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

import com.nhl.link.rest.CreateOrUpdateBuilder;
import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.runtime.CreateOrUpdateOperation;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.dao.EntityDao;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class CayenneDao<T> implements EntityDao<T> {

	private Class<T> type;
	private ICayennePersister persister;
	private IEncoderService encoderService;
	private IRequestParser requestParser;
	private IConstraintsHandler constraintsHandler;
	private IMetadataService metadataService;

	public CayenneDao(Class<T> type, IRequestParser requestParser, IEncoderService encoderService,
			ICayennePersister cayenneService, IConstraintsHandler constraintsHandler, IMetadataService metadataService) {
		this.type = type;
		this.requestParser = requestParser;
		this.encoderService = encoderService;
		this.persister = cayenneService;
		this.constraintsHandler = constraintsHandler;
		this.metadataService = metadataService;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public SelectBuilder<T> forSelect() {
		return new CayenneSelectBuilder<T>(type, persister, encoderService, requestParser, constraintsHandler);
	}

	@Override
	public SelectBuilder<T> forSelect(SelectQuery<T> query) {
		return new CayenneSelectBuilder<T>(query, type, persister, encoderService, requestParser, constraintsHandler);
	}

	@Override
	public CreateOrUpdateBuilder<T> create() {
		return new CayenneCreateOrUpdateBuilder<>(type, CreateOrUpdateOperation.create, persister, encoderService,
				requestParser, metadataService, constraintsHandler);
	}

	@Override
	public CreateOrUpdateBuilder<T> createOrUpdate() {
		return new CayenneCreateOrUpdateBuilder<>(type, CreateOrUpdateOperation.createOrUpdate, persister,
				encoderService, requestParser, metadataService, constraintsHandler);
	}

	@Override
	public CreateOrUpdateBuilder<T> idempotentCreateOrUpdate() {
		return new CayenneCreateOrUpdateBuilder<>(type, CreateOrUpdateOperation.idempotentCreateOrUpdate, persister,
				encoderService, requestParser, metadataService, constraintsHandler);
	}

	@Override
	public CreateOrUpdateBuilder<T> update() {
		return new CayenneCreateOrUpdateBuilder<>(type, CreateOrUpdateOperation.update, persister, encoderService,
				requestParser, metadataService, constraintsHandler);
	}

	@Override
	public DeleteBuilder<T> delete() {
		return new CayenneDeleteBuilder<>(type, persister);
	}

	// TODO: use ObjectMapper
	private <A> A getExistingObject(Class<A> type, ObjectContext context, Object id) {

		A object = getOptionalExistingObject(type, context, id);
		if (object == null) {
			ObjEntity entity = context.getEntityResolver().getObjEntity(type);
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
					+ entity.getName() + "'");
		}

		return object;
	}

	// TODO: use ObjectMapper
	@SuppressWarnings("unchecked")
	private <A> A getOptionalExistingObject(Class<A> type, ObjectContext context, Object id) {

		ObjEntity entity = context.getEntityResolver().getObjEntity(type);

		// sanity checking...
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
		}

		// TODO: should we start using optimistic locking on PK by default
		// instead of SELECT/DELETE|UPDATE?

		String idName = entity.getPrimaryKeyNames().iterator().next();
		ObjectIdQuery select = new ObjectIdQuery(new ObjectId(entity.getName(), idName, id));

		return (A) Cayenne.objectForQuery(context, select);
	}

	@Override
	public void unrelate(Object sourceId, String relationship) {

		// validate relationship before doing anything else
		ObjRelationship objRelationship = metadataService.getObjRelationship(type, relationship);

		ObjectContext context = persister.newContext();
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

		ObjectContext context = persister.newContext();

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

}
