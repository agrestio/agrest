package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectIdQuery;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;

/**
 * @since 1.16
 */
public class CayenneUnrelateStage<T> extends BaseLinearProcessingStage<UnrelateContext<T>, T> {

	private IMetadataService metadataService;

	public CayenneUnrelateStage(ProcessingStage<UnrelateContext<T>, ? super T> next, IMetadataService metadataService) {
		super(next);
		this.metadataService = metadataService;
	}

	@Override
	protected void doExecute(UnrelateContext<T> context) {

		ObjectContext cayenneContext = CayenneContextInitStage.cayenneContext(context);

		if (context.getId() != null) {
			unrelateSingle(context, cayenneContext);
		} else {
			unrelateAll(context, cayenneContext);
		}
	}

	private void unrelateSingle(UnrelateContext<?> context, ObjectContext cayenneContext) {

		// validate relationship before doing anything else
		LrRelationship relationship = metadataService.getLrRelationship(context.getParent());

		DataObject parent = (DataObject) getExistingObject(context.getParent().getType(), cayenneContext, context
				.getParent().getId());

		Class<?> childType = metadataService.getLrEntity(relationship.getTargetEntityType()).getType();

		// among other things this call checks that the target exists
		DataObject child = (DataObject) getExistingObject(childType, cayenneContext, context.getId());

		if (relationship.isToMany()) {

			// sanity check...
			Collection<?> relatedCollection = (Collection<?>) parent.readProperty(relationship.getName());
			if (!relatedCollection.contains(child)) {
				throw new LinkRestException(Status.EXPECTATION_FAILED, "Source and target are not related");
			}

			parent.removeToManyTarget(relationship.getName(), child, true);
		} else {

			// sanity check...
			if (parent.readProperty(relationship.getName()) != child) {
				throw new LinkRestException(Status.EXPECTATION_FAILED, "Source and target are not related");
			}

			parent.setToOneTarget(relationship.getName(), null, true);
		}

		cayenneContext.commitChanges();
	}

	private void unrelateAll(UnrelateContext<?> context, ObjectContext cayenneContext) {
		// validate relationship before doing anything else
		LrRelationship lrRelationship = metadataService.getLrRelationship(context.getParent());

		DataObject parent = (DataObject) getExistingObject(context.getParent().getType(), cayenneContext, context
				.getParent().getId());

		if (lrRelationship.isToMany()) {

			// clone relationship before we start deleting to avoid concurrent
			// modification of the iterator, and to be able to batch-delete
			// objects if needed
			@SuppressWarnings("unchecked")
			Collection<DataObject> relatedCollection = new ArrayList<>(
					(Collection<DataObject>) parent.readProperty(lrRelationship.getName()));

			for (DataObject o : relatedCollection) {
				parent.removeToManyTarget(lrRelationship.getName(), o, true);
			}

		} else {

			DataObject target = (DataObject) parent.readProperty(lrRelationship.getName());
			if (target != null) {
				parent.setToOneTarget(lrRelationship.getName(), null, true);
			}
		}

		cayenneContext.commitChanges();
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
}
