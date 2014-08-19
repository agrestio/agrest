package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.ObjectIdQuery;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.BaseCreateOrUpdateBuilder;
import com.nhl.link.rest.runtime.CreateOrUpdateOperation;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * @since 3.2
 */
class CayenneCreateOrUpdateBuilder<T> extends BaseCreateOrUpdateBuilder<T> {

	private ICayennePersister persister;

	public CayenneCreateOrUpdateBuilder(Class<T> type, CreateOrUpdateOperation op, ICayennePersister persister,
			IEncoderService encoderService, IRequestParser requestParser, IMetadataService metadataService,
			IConstraintsHandler constraintsHandler) {
		super(type, op, encoderService, requestParser, metadataService, constraintsHandler);

		this.persister = persister;
		this.metadataService = metadataService;
	}

	protected List<T> process(UpdateResponse<T> response, ObjectLocator<T> locator) {

		int len = response.getUpdates().size();
		if (len == 0) {
			return Collections.emptyList();
		}

		ObjEntity entity = response.getEntity().getCayenneEntity();
		ObjectContext context = persister.newContext();
		ObjectRelator<T> relator = createRelator(context);
		List<T> created = new ArrayList<>(len);

		for (EntityUpdate u : response.getUpdates()) {
			T o = locator.locate(u, context, entity);
			mergeChanges(entity, u, (DataObject) o);
			relator.relate(o);
			created.add(o);
		}

		context.commitChanges();

		return created;
	}

	protected ObjectRelator<T> createRelator(ObjectContext context) {

		if (parentType == null) {
			return new ObjectRelator<T>() {

				@Override
				public void relate(T object) {
					// do nothing..
				}
			};
		}

		final DataObject parent = (DataObject) getExistingObject(parentType, context, parentId);

		// TODO: check that relationship target is the same as <T> ??
		if (metadataService.getObjRelationship(parentType, relationshipFromParent).isToMany()) {
			return new ObjectRelator<T>() {
				@Override
				public void relate(T object) {
					parent.addToManyTarget(relationshipFromParent, (DataObject) object, true);
				}
			};
		} else {
			return new ObjectRelator<T>() {
				@Override
				public void relate(T object) {
					parent.setToOneTarget(relationshipFromParent, (DataObject) object, true);
				}
			};
		}
	}

	@Override
	protected List<T> create(UpdateResponse<T> response) {

		return process(response, new ObjectLocator<T>() {

			@Override
			public T locate(EntityUpdate u, ObjectContext c, ObjEntity entity) {
				return createObject(c, entity, u.getId());
			}

		});
	}

	@Override
	protected List<T> update(UpdateResponse<T> response) {
		return process(response, new ObjectLocator<T>() {

			@Override
			public T locate(EntityUpdate u, ObjectContext c, ObjEntity entity) {
				return getExistingObject(type, c, u.getId());
			}

		});
	}

	@Override
	protected List<T> createOrUpdate(UpdateResponse<T> response) {
		return process(response, new ObjectLocator<T>() {

			@Override
			public T locate(EntityUpdate u, ObjectContext c, ObjEntity entity) {
				T o = u.getId() != null ? getOptionalExistingObject(type, c, u.getId()) : null;
				return o != null ? o : createObject(c, entity, u.getId());
			}
		});
	}

	@Override
	protected List<T> idempotentCreateOrUpdate(UpdateResponse<T> response) {
		return process(response, new ObjectLocator<T>() {

			@Override
			public T locate(EntityUpdate u, ObjectContext c, ObjEntity entity) {

				if (u.getId() == null) {
					throw new LinkRestException(Status.BAD_REQUEST,
							"Request is not idempotent. At least one update has no id");
				}

				T o = getOptionalExistingObject(type, c, u.getId());
				return o != null ? o : createObject(c, entity, u.getId());
			}
		});
	}

	private T createObject(ObjectContext context, ObjEntity entity, Object id) {

		T o = context.newObject(type);

		// set explicit ID
		if (id != null) {

			// TODO: compile ID strategy to avoid recalculating metadata all the
			// time...

			Collection<DbAttribute> pks = entity.getDbEntity().getPrimaryKeys();
			if (pks.size() != 1) {
				throw new IllegalStateException(String.format("Unexpected PK size of %s for entity '%s'",
						entity.getName(), pks.size()));
			}

			DataObject dataObject = (DataObject) o;

			DbAttribute pk = pks.iterator().next();
			// reuse the ID...
			// 1. meaningful ID
			ObjAttribute opk = entity.getAttributeForDbAttribute(pk);
			if (opk != null) {
				dataObject.writeProperty(opk.getName(), id);
			}
			// 2. PK is propagated from the parent
			// else if () {}
			//
			// 3. PK is auto-generated ... I guess this is sorta
			// expected to fail - generated meaningless PK should not be
			// pushed from the client
			else if (pk.isGenerated()) {
				throw new LinkRestException(Status.BAD_REQUEST, "Can't create '" + entity.getName() + "' with fixed id");
			}
			// 4. just some ID desired by the client...
			else {
				// TODO: hopefully this works..
				dataObject.getObjectId().getReplacementIdMap().put(pk.getName(), id);
			}
		}

		return o;
	}

	private void mergeChanges(ObjEntity entity, EntityUpdate entityUpdate, DataObject object) {

		// attributes
		for (Entry<String, Object> e : entityUpdate.getValues().entrySet()) {
			object.writeProperty(e.getKey(), e.getValue());
		}

		// to-one relationships
		if (!entityUpdate.getRelatedIds().isEmpty()) {
			ObjectContext context = object.getObjectContext();
			for (Entry<String, Object> e : entityUpdate.getRelatedIds().entrySet()) {
				if (e.getValue() == null) {
					object.setToOneTarget(e.getKey(), null, true);
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
				object.setToOneTarget(e.getKey(), related, true);
			}
		}
	}

	private <A> A getExistingObject(Class<A> type, ObjectContext context, Object id) {

		A object = getOptionalExistingObject(type, context, id);
		if (object == null) {
			ObjEntity entity = context.getEntityResolver().getObjEntity(type);
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
					+ entity.getName() + "'");
		}

		return object;
	}

	@SuppressWarnings("unchecked")
	private <A> A getOptionalExistingObject(Class<A> type, ObjectContext context, Object id) {

		ObjEntity entity = context.getEntityResolver().getObjEntity(type);

		// sanity checking...
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
		}

		if (id == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No id specified");
		}

		// TODO: should we start using optimistic locking on PK by default
		// instead of SELECT/DELETE|UPDATE?

		String idName = entity.getPrimaryKeyNames().iterator().next();
		ObjectIdQuery select = new ObjectIdQuery(new ObjectId(entity.getName(), idName, id));

		return (A) Cayenne.objectForQuery(context, select);
	}

	// TODO: Java 8 functional interface candidates
	interface ObjectLocator<T> {

		T locate(EntityUpdate u, ObjectContext c, ObjEntity entity);
	}

	interface ObjectRelator<T> {

		void relate(T object);
	}
}
