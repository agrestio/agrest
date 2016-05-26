package com.nhl.link.rest.runtime.cayenne.processor;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.annotation.listener.DataStoreUpdated;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public abstract class BaseCayenneUpdateStage<T extends DataObject> extends BaseLinearProcessingStage<UpdateContext<T>, T> {

	private IMetadataService metadataService;

	public BaseCayenneUpdateStage(ProcessingStage<UpdateContext<T>, ? super T> next, IMetadataService metadataService) {
		super(next);
		this.metadataService = metadataService;
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
			mergeChanges(u, o, relator);
		}

		relator.relateToParent(o);
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

			if (isPrimaryKey(entity.getDbEntity(), idMap.keySet())) {
				// lrId is the same as the PK,
				// no need to make an additional check that the lrId is unique
				for (DbAttribute pk : entity.getDbEntity().getPrimaryKeys()) {
					Object id = idMap.get(pk.getName());
					if (id == null) {
						continue;
					}
					setPrimaryKey(o, entity, pk, id);
				}

			} else {
				// need to make an additional check that the lrId is unique
				// TODO: I guess this should be done in a separate new context
				T existing = Util.findById(objectContext, context.getType(), context.getEntity().getLrEntity(), idMap);
				if (existing != null) {
					throw new LinkRestException(Status.BAD_REQUEST, "Can't create '" + entity.getName()
								+ "' with id " + LrObjectId.mapToString(idMap) + " -- object already exists");
				}

				for (Map.Entry<String, Object> idPart : idMap.entrySet()) {

					DbAttribute pk = null;
					for (DbAttribute _pk: entity.getDbEntity().getPrimaryKeys()) {
						if (_pk.getName().equals(idPart.getKey())) {
							pk = _pk;
							break;
						}
					}

					if (pk == null) {
						DbAttribute dbAttribute = entity.getDbEntity().getAttribute(idPart.getKey());
						if (dbAttribute == null) {
							throw new LinkRestException(Status.BAD_REQUEST, "Can't create '" + entity.getName()
								+ "' with id " + LrObjectId.mapToString(idMap) + " -- unknown db attribute: " + idPart.getKey());
						}
						ObjAttribute objAttribute = entity.getAttributeForDbAttribute(dbAttribute);
						if (objAttribute == null) {
							throw new LinkRestException(Status.BAD_REQUEST, "Can't create '" + entity.getName()
								+ "' with id " + LrObjectId.mapToString(idMap) + " -- unknown object attribute: " + idPart.getKey());
						}
						o.writeProperty(objAttribute.getName(), idPart.getValue());
					} else {
						setPrimaryKey(o, entity, pk, idPart.getValue());
					}
				}
			}
		}

		mergeChanges(u, o, relator);
		relator.relateToParent(o);
	}

	private void setPrimaryKey(DataObject o, ObjEntity entity, DbAttribute pk, Object id) {

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

	/**
	 * @return true if all PK columns are represented in {@code keys}
     */
	private boolean isPrimaryKey(DbEntity entity, Collection<String> keys) {
		Collection<DbAttribute> pks = entity.getPrimaryKeys();
		for (DbAttribute pk : pks) {
			if (!keys.contains(pk.getName())) {
				return false;
			}
		}
		return true;
	}

	private void mergeChanges(EntityUpdate<T> entityUpdate, DataObject o, ObjectRelator relator) {

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

			final Set<Object> relatedIds = e.getValue();
			if (relatedIds == null || relatedIds.isEmpty() || allElementsNull(relatedIds)) {

				relator.unrelateAll(lrRelationship, o);
				continue;
			}

			if (!lrRelationship.isToMany() && relatedIds.size() > 1) {
				throw new LinkRestException(Status.BAD_REQUEST,
					"Relationship is to-one, but received update with multiple objects: " +
							lrRelationship.getName());
			}

			ClassDescriptor relatedDescriptor = context.getEntityResolver().getClassDescriptor(
					relationship.getTargetEntityName());

			relator.unrelateAll(lrRelationship, o, new RelationshipUpdate() {
				@Override
				public boolean containsRelatedObject(DataObject relatedObject) {
					return relatedIds.contains(Cayenne.pkForObject(relatedObject));
				}

				@Override
				public void removeUpdateForRelatedObject(DataObject relatedObject) {
					relatedIds.remove(Cayenne.pkForObject(relatedObject));
				}
			});

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

				relator.relate(lrRelationship, o, related);
			}
		}

		// record this for the benefit of the downstream code that may want to
		// order the results, etc...
		entityUpdate.setMergedTo(o);
	}

	private boolean allElementsNull(Collection<?> elements) {

		for (Object element : elements) {
			if (element != null) {
				return false;
			}
		}

		return true;
	}

	protected ObjectRelator createRelator(UpdateContext<T> context) {

		final EntityParent<?> parent = context.getParent();

		if (parent == null) {
			return new ObjectRelator();
		}

		ObjectContext objectContext = CayenneContextInitStage.cayenneContext(context);

		ObjEntity parentEntity = objectContext.getEntityResolver().getObjEntity(parent.getType());
		LrEntity<?> parentLrEntity = metadataService.getLrEntity(context.getParent().getType());
		final DataObject parentObject = (DataObject) Util.findById(objectContext, parent.getType(),
				parentLrEntity, parent.getId().get());

		if (parentObject == null) {
			throw new LinkRestException(Status.NOT_FOUND, "No parent object for ID '" + parent.getId()
					+ "' and entity '" + parentEntity.getName() + "'");
		}

		// TODO: check that relationship target is the same as <T> ??
		if (parentEntity.getRelationship(parent.getRelationship()).isToMany()) {
			return new ObjectRelator() {
				@Override
				public void relateToParent(DataObject object) {
					parentObject.addToManyTarget(parent.getRelationship(), (DataObject) object, true);
				}
			};
		} else {
			return new ObjectRelator() {
				@Override
				public void relateToParent(DataObject object) {
					parentObject.setToOneTarget(parent.getRelationship(), (DataObject) object, true);
				}
			};
		}
	}

	class ObjectRelator {

		void relateToParent(DataObject object) {
			// do nothing
		}

		void relate(LrRelationship lrRelationship, DataObject object, DataObject relatedObject) {
			if (lrRelationship.isToMany()) {
				object.addToManyTarget(lrRelationship.getName(), relatedObject, true);
			} else {
				object.setToOneTarget(lrRelationship.getName(), relatedObject, true);
			}
		}

		void unrelateAll(LrRelationship lrRelationship, DataObject object) {
			unrelateAll(lrRelationship, object, null);
		}

		void unrelateAll(LrRelationship lrRelationship, DataObject object, RelationshipUpdate relationshipUpdate) {

			if (lrRelationship.isToMany()) {

				@SuppressWarnings("unchecked")
				List<? extends DataObject> relatedObjects =
						(List<? extends DataObject>) object.readProperty(lrRelationship.getName());

				for (int i = 0; i < relatedObjects.size(); i++) {
					DataObject relatedObject = relatedObjects.get(i);
					if (relationshipUpdate == null || !relationshipUpdate.containsRelatedObject(relatedObject)) {
						object.removeToManyTarget(lrRelationship.getName(), relatedObject, true);
						i--;
					} else {
						relationshipUpdate.removeUpdateForRelatedObject(relatedObject);
					}
				}

			} else {
				object.setToOneTarget(lrRelationship.getName(), null, true);
			}
		}
	}

	interface RelationshipUpdate {
		boolean containsRelatedObject(DataObject o);
		void removeUpdateForRelatedObject(DataObject o);
	}
}
