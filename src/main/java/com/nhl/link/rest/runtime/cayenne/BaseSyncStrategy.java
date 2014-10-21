package com.nhl.link.rest.runtime.cayenne;

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

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.cayenne.CayenneUpdateBuilder.ObjectRelator;

/**
 * @since 1.7
 */
abstract class BaseSyncStrategy<T> implements SyncStrategy<T> {

	protected CayenneUpdateResponse<T> response;
	protected ObjectRelator<T> relator;
	protected ObjEntity entity;

	BaseSyncStrategy(CayenneUpdateResponse<T> response, ObjectRelator<T> relator) {
		this.response = response;
		this.relator = relator;
		this.entity = response.getEntity().getCayenneEntity();
	}

	@Override
	public void sync() {
		doSync();
		response.getUpdateContext().commitChanges();
	}

	protected abstract void doSync();

	protected T create(Collection<EntityUpdate> updates) {

		T o = null;
		for (EntityUpdate u : updates) {
			if (o == null) {

				// update 0
				o = doCreate(u);
				mergeChanges(u, o);
				relator.relate(o);
			} else {

				// updates 1..N simply merge into the existing object
				mergeChanges(u, o);
			}
		}

		return o;
	}

	protected void update(Collection<EntityUpdate> updates, T o) {

		for (EntityUpdate u : updates) {
			mergeChanges(u, o);
		}

		relator.relate(o);
	}

	private void mergeChanges(EntityUpdate entityUpdate, T o) {

		DataObject dataO = (DataObject) o;

		// attributes
		for (Entry<String, Object> e : entityUpdate.getValues().entrySet()) {
			dataO.writeProperty(e.getKey(), e.getValue());
		}

		// to-one relationships
		if (!entityUpdate.getRelatedIds().isEmpty()) {
			ObjectContext context = dataO.getObjectContext();
			for (Entry<String, Object> e : entityUpdate.getRelatedIds().entrySet()) {
				if (e.getValue() == null) {
					dataO.setToOneTarget(e.getKey(), null, true);
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
				dataO.setToOneTarget(e.getKey(), related, true);
			}
		}

		// record this for the benefit of the downstream code that may want to
		// order the results, etc...
		entityUpdate.setMergedTo(dataO);
	}

	private T doCreate(EntityUpdate u) {

		T o = response.getUpdateContext().newObject(response.getType());
		Map<String, Object> idMap = u.getId();

		// set explicit ID
		if (idMap != null) {

			if (response.isIdUpdatesDisallowed() && u.isExplicitId()) {
				throw new LinkRestException(Status.BAD_REQUEST, "Setting ID explicitly is not allowed: " + idMap);
			}

			DataObject dataObject = (DataObject) o;

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
					dataObject.writeProperty(opk.getName(), id);
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
					dataObject.getObjectId().getReplacementIdMap().put(pk.getName(), id);
				}
			}
		}

		return o;
	}

}
