package com.nhl.link.rest.runtime.cayenne.processor;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.meta.LrRelationship;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.annotation.listener.DataStoreUpdated;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public abstract class BaseCayenneUpdateStage<T extends DataObject> extends BaseLinearProcessingStage<UpdateContext<T>, T> {

	public BaseCayenneUpdateStage(ProcessingStage<UpdateContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void doExecute(UpdateContext<T> context) {
		sync(context);
		CayenneContextInitStage.cayenneContext(context).commitChanges();
	}
	
	@Override
	public Class<? extends Annotation> afterStageListener() {
		return DataStoreUpdated.class;
	}

	protected abstract void sync(UpdateContext<T> context);

	protected void create(UpdateContext<T> context) {

		ObjectRelator relator = createRelator(context);

		for (EntityUpdate<T> u : context.getUpdates()) {
			createSingle(context, relator, u);
		}
	}

	protected void updateSingle(UpdateContext<T> context, T o, Collection<EntityUpdate<T>> updates) {

		ObjectRelator relator = createRelator(context);

		for (EntityUpdate<T> u : updates) {
			mergeChanges(u, o);
		}

		relator.relate(o);
	}

	protected void createSingle(UpdateContext<T> context, ObjectRelator relator, EntityUpdate<T> u) {

		ObjectContext objectContext = CayenneContextInitStage.cayenneContext(context);
		DataObject o = objectContext.newObject(context.getType());
		Map<String, Object> idMap = u.getId();

		// set explicit ID
		if (idMap != null) {

			if (context.isIdUpdatesDisallowed() && u.isExplicitId()) {
				throw new LinkRestException(Status.BAD_REQUEST, "Setting ID explicitly is not allowed: " + idMap);
			}

			ObjEntity entity = objectContext.getEntityResolver().getObjEntity(context.getType());

			for (DbAttribute pk : entity.getDbEntity().getPrimaryKeys()) {

				Object id = idMap.get(pk.getName());
				if (id == null) {
					continue;
				}

				// 1. meaningful ID
				// TODO: must compile all this... figuring this on the fly is
				// slow
				ObjAttribute opk = entity.getAttributeForDbAttribute(pk);
				if (opk != null) {
					o.writeProperty(opk.getName(), id);
				}
				// 2. PK is auto-generated ... I guess this is sorta
				// expected to fail - generated meaningless PK should not be
				// pushed from the client
				else if (pk.isGenerated()) {
					throw new LinkRestException(Status.BAD_REQUEST, "Can't create '" + entity.getName()
							+ "' with fixed id");
				}
				// 3. probably a propagated ID.
				else {
					// TODO: hopefully this works..
					o.getObjectId().getReplacementIdMap().put(pk.getName(), id);
				}
			}
		}

		mergeChanges(u, o);
		relator.relate(o);
	}

	private void mergeChanges(EntityUpdate<T> entityUpdate, DataObject o) {

		// attributes
		for (Entry<String, Object> e : entityUpdate.getValues().entrySet()) {
			o.writeProperty(e.getKey(), e.getValue());
		}

		// relationships
		ObjectContext context = o.getObjectContext();

		ObjEntity entity = context.getEntityResolver().getObjEntity(o);

		for (Entry<String, Set<Object>> e : entityUpdate.getRelatedIds().entrySet()) {

			ObjRelationship relationship = entity.getRelationship(e.getKey());
			LrRelationship lrRelationship = entityUpdate.getEntity().getRelationship(e.getKey());

			// sanity check
			if (lrRelationship == null) {
				continue;
			}

			Set<Object> relatedIds = e.getValue();
			if (relatedIds == null || relatedIds.isEmpty()
					|| (relatedIds.size() == 1 && relatedIds.iterator().next() == null)) {

				if (lrRelationship.isToMany()) {

					// removing all related objects
					@SuppressWarnings("unchecked")
					List<? extends DataObject> relatedObjects = (List<? extends DataObject>) o.readProperty(lrRelationship.getName());
					while (!relatedObjects.isEmpty()) {
						o.removeToManyTarget(e.getKey(), relatedObjects.get(0), true);
					}
				} else {
					o.setToOneTarget(e.getKey(), null, true);
				}
				continue;
			}

			if (!lrRelationship.isToMany() && relatedIds.size() > 1) {
				throw new LinkRestException(Status.BAD_REQUEST,
					"Relationship is to-one, but received update with multiple objects: " +
							lrRelationship.getName());
			}

			ClassDescriptor relatedDescriptor = context.getEntityResolver().getClassDescriptor(
					relationship.getTargetEntityName());

			if (lrRelationship.isToMany()) {

				@SuppressWarnings("unchecked")
				List<? extends DataObject> relatedObjects = (List<? extends DataObject>) o.readProperty(lrRelationship.getName());
				for (int i = 0; i < relatedObjects.size(); i++) {
					DataObject relatedObject = relatedObjects.get(i);
					if (!relatedIds.contains(Cayenne.pkForObject(relatedObject))) {
						o.removeToManyTarget(e.getKey(), relatedObject, true);
						i--;
					}
				}
			}

			for (Object relatedId : relatedIds) {

				if (relatedId == null) {
					continue;
				}

				DataObject related = (DataObject) Cayenne.objectForPK(context, relatedDescriptor.getObjectClass(),
						relatedId);

				if (related == null) {
					throw new LinkRestException(Status.NOT_FOUND, "Related object '"
							+ relationship.getTargetEntityName() + "' with ID '" + e.getValue() + "' is not found");
				}

				if (lrRelationship.isToMany()) {
					o.addToManyTarget(e.getKey(), related, true);
				} else {
					o.setToOneTarget(e.getKey(), related, true);
				}
			}
		}

		// record this for the benefit of the downstream code that may want to
		// order the results, etc...
		entityUpdate.setMergedTo(o);
	}

	protected ObjectRelator createRelator(UpdateContext<T> context) {

		final EntityParent<?> parent = context.getParent();

		if (parent == null) {
			return new ObjectRelator() {

				@Override
				public void relate(DataObject object) {
					// do nothing..
				}
			};
		}

		ObjectContext objectContext = CayenneContextInitStage.cayenneContext(context);

		ObjEntity parentEntity = objectContext.getEntityResolver().getObjEntity(parent.getType());
		final DataObject parentObject = (DataObject) Util.findById(objectContext, parent.getType(), parent.getId().get());

		if (parentObject == null) {
			throw new LinkRestException(Status.NOT_FOUND, "No parent object for ID '" + parent.getId()
					+ "' and entity '" + parentEntity.getName() + "'");
		}

		// TODO: check that relationship target is the same as <T> ??
		if (parentEntity.getRelationship(parent.getRelationship()).isToMany()) {
			return new ObjectRelator() {
				@Override
				public void relate(DataObject object) {
					parentObject.addToManyTarget(parent.getRelationship(), (DataObject) object, true);
				}
			};
		} else {
			return new ObjectRelator() {
				@Override
				public void relate(DataObject object) {
					parentObject.setToOneTarget(parent.getRelationship(), (DataObject) object, true);
				}
			};
		}
	}

	interface ObjectRelator {
		void relate(DataObject object);
	}
}
