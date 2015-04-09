package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

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
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public abstract class BaseCayenneUpdateStage extends ProcessingStage<UpdateContext<?>> {

	public BaseCayenneUpdateStage(Processor<UpdateContext<?>> next) {
		super(next);
	}

	@Override
	protected void doExecute(UpdateContext<?> context) {
		sync(context);
		CayenneContextInitStage.cayenneContext(context).commitChanges();
	}

	protected abstract <T> void sync(UpdateContext<T> context);

	protected void create(UpdateContext<?> context) {

		ObjectRelator relator = createRelator(context);

		for (EntityUpdate u : context.getResponse().getUpdates()) {
			createSingle(context, relator, u);
		}
	}

	protected <T> void updateSingle(UpdateContext<T> context, T o, Collection<EntityUpdate> updates) {

		DataObject dataO = (DataObject) o;
		ObjectRelator relator = createRelator(context);

		for (EntityUpdate u : updates) {
			mergeChanges(u, dataO);
		}

		relator.relate(dataO);
	}

	protected void createSingle(UpdateContext<?> context, ObjectRelator relator, EntityUpdate u) {

		ObjectContext objectContext = CayenneContextInitStage.cayenneContext(context);
		DataObject o = (DataObject) objectContext.newObject(context.getType());
		Map<String, Object> idMap = u.getId();

		// set explicit ID
		if (idMap != null) {

			if (context.getResponse().isIdUpdatesDisallowed() && u.isExplicitId()) {
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

	private void mergeChanges(EntityUpdate entityUpdate, DataObject o) {

		// attributes
		for (Entry<String, Object> e : entityUpdate.getValues().entrySet()) {
			o.writeProperty(e.getKey(), e.getValue());
		}

		// to-one relationships
		if (!entityUpdate.getRelatedIds().isEmpty()) {
			ObjectContext context = o.getObjectContext();

			ObjEntity entity = context.getEntityResolver().getObjEntity(o);

			for (Entry<String, Object> e : entityUpdate.getRelatedIds().entrySet()) {
				if (e.getValue() == null) {
					o.setToOneTarget(e.getKey(), null, true);
					continue;
				}
				ObjRelationship relationship = (ObjRelationship) entity.getRelationship(e.getKey());
				ClassDescriptor relatedDescriptor = context.getEntityResolver().getClassDescriptor(
						relationship.getTargetEntityName());
				DataObject related = (DataObject) Cayenne.objectForPK(context, relatedDescriptor.getObjectClass(),
						e.getValue());
				if (related == null) {
					throw new LinkRestException(Status.NOT_FOUND, "Related object '"
							+ relationship.getTargetEntityName() + "' with ID '" + e.getValue() + "' is not found");
				}
				o.setToOneTarget(e.getKey(), related, true);
			}
		}

		// record this for the benefit of the downstream code that may want to
		// order the results, etc...
		entityUpdate.setMergedTo(o);
	}

	protected ObjectRelator createRelator(UpdateContext<?> context) {

		final EntityParent<?> parent = context.getResponse().getParent();

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
		final DataObject parentObject = (DataObject) Util.findById(objectContext, parent.getType(), parent.getId());

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
