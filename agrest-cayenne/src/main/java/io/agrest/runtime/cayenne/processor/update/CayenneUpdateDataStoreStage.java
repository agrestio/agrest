package io.agrest.runtime.cayenne.processor.update;

import io.agrest.CompoundObjectId;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.AgException;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.cayenne.processor.Util;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.reflect.ClassDescriptor;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 2.7
 */
public abstract class CayenneUpdateDataStoreStage implements Processor<UpdateContext<?, ?>> {

    private IMetadataService metadataService;

    public CayenneUpdateDataStoreStage(IMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?, ?> context) {
        sync((UpdateContext<DataObject, Expression>) context);
        CayenneUpdateStartStage.cayenneContext(context).commitChanges();
        return ProcessorOutcome.CONTINUE;
    }

    protected abstract <T extends DataObject> void sync(UpdateContext<T, Expression> context);

    protected <T extends DataObject> void create(UpdateContext<T, Expression> context) {

        ObjectRelator relator = createRelator(context);

        for (EntityUpdate<T> u : context.getUpdates()) {
            createSingle(context, relator, u);
        }
    }

    protected <T extends DataObject> void updateSingle(UpdateContext<T, Expression> context, T o, Collection<EntityUpdate<T>> updates) {

        ObjectRelator relator = createRelator(context);

        for (EntityUpdate<T> u : updates) {
            mergeChanges(u, o, relator);
        }

        relator.relateToParent(o);
    }

    protected <T extends DataObject> void createSingle(UpdateContext<T, Expression> context, ObjectRelator relator, EntityUpdate<T> u) {

        ObjectContext objectContext = CayenneUpdateStartStage.cayenneContext(context);
        DataObject o = objectContext.newObject(context.getType());
        Map<String, Object> idMap = u.getId();

        // set explicit ID
        if (idMap != null) {

            if (context.isIdUpdatesDisallowed() && u.isExplicitId()) {
                throw new AgException(Response.Status.BAD_REQUEST, "Setting ID explicitly is not allowed: " + idMap);
            }

            ObjEntity entity = objectContext.getEntityResolver().getObjEntity(context.getType());

            if (isPrimaryKey(entity.getDbEntity(), idMap.keySet())) {
                // AgId is the same as the PK,
                // no need to make an additional check that the AGId is unique
                for (DbAttribute pk : entity.getDbEntity().getPrimaryKeys()) {
                    Object id = idMap.get(pk.getName());
                    if (id == null) {
                        continue;
                    }
                    setPrimaryKey(o, entity, pk, id);
                }

            } else {
                // need to make an additional check that the AgId is unique
                // TODO: I guess this should be done in a separate new context
                T existing = Util.findById(objectContext, context.getType(), context.getEntity().getAgEntity(), idMap);
                if (existing != null) {
                    throw new AgException(Response.Status.BAD_REQUEST, "Can't create '" + entity.getName()
                            + "' with id " + CompoundObjectId.mapToString(idMap) + " -- object already exists");
                }

                for (Map.Entry<String, Object> idPart : idMap.entrySet()) {

                    DbAttribute pk = null;
                    for (DbAttribute _pk : entity.getDbEntity().getPrimaryKeys()) {
                        if (_pk.getName().equals(idPart.getKey())) {
                            pk = _pk;
                            break;
                        }
                    }

                    if (pk == null) {
                        DbAttribute dbAttribute = entity.getDbEntity().getAttribute(idPart.getKey());
                        if (dbAttribute == null) {
                            throw new AgException(Response.Status.BAD_REQUEST, "Can't create '" + entity.getName()
                                    + "' with id " + CompoundObjectId.mapToString(idMap) + " -- unknown db attribute: " + idPart.getKey());
                        }
                        ObjAttribute objAttribute = entity.getAttributeForDbAttribute(dbAttribute);
                        if (objAttribute == null) {
                            throw new AgException(Response.Status.BAD_REQUEST, "Can't create '" + entity.getName()
                                    + "' with id " + CompoundObjectId.mapToString(idMap) + " -- unknown object attribute: " + idPart.getKey());
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
            throw new AgException(Response.Status.BAD_REQUEST, "Can't create '" + entity.getName()
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

    private <T extends DataObject> void mergeChanges(EntityUpdate<T> entityUpdate, DataObject o, ObjectRelator relator) {

        // attributes
        for (Map.Entry<String, Object> e : entityUpdate.getValues().entrySet()) {
            o.writeProperty(e.getKey(), e.getValue());
        }

        // relationships
        ObjectContext context = o.getObjectContext();

        ObjEntity entity = context.getEntityResolver().getObjEntity(o);

        for (Map.Entry<String, Set<Object>> e : entityUpdate.getRelatedIds().entrySet()) {

            ObjRelationship relationship = entity.getRelationship(e.getKey());
            AgRelationship agRelationship = entityUpdate.getEntity().getRelationship(e.getKey());

            // sanity check
            if (agRelationship == null) {
                continue;
            }

            final Set<Object> relatedIds = e.getValue();
            if (relatedIds == null || relatedIds.isEmpty() || allElementsNull(relatedIds)) {

                relator.unrelateAll(agRelationship, o);
                continue;
            }

            if (!agRelationship.isToMany() && relatedIds.size() > 1) {
                throw new AgException(Response.Status.BAD_REQUEST,
                        "Relationship is to-one, but received update with multiple objects: " +
                                agRelationship.getName());
            }

            ClassDescriptor relatedDescriptor = context.getEntityResolver().getClassDescriptor(
                    relationship.getTargetEntityName());

            relator.unrelateAll(agRelationship, o, new RelationshipUpdate() {
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
                    throw new AgException(Response.Status.NOT_FOUND, "Related object '"
                            + relationship.getTargetEntityName() + "' with ID '" + e.getValue() + "' is not found");
                }

                relator.relate(agRelationship, o, related);
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

    protected <T extends DataObject> ObjectRelator createRelator(UpdateContext<T, Expression> context) {

        final EntityParent<?> parent = context.getParent();

        if (parent == null) {
            return new ObjectRelator();
        }

        ObjectContext objectContext = CayenneUpdateStartStage.cayenneContext(context);

        ObjEntity parentEntity = objectContext.getEntityResolver().getObjEntity(parent.getType());
        AgEntity<?> parentAgEntity = metadataService.getAgEntity(context.getParent().getType());
        final DataObject parentObject = (DataObject) Util.findById(objectContext, parent.getType(),
                parentAgEntity, parent.getId().get());

        if (parentObject == null) {
            throw new AgException(Response.Status.NOT_FOUND, "No parent object for ID '" + parent.getId()
                    + "' and entity '" + parentEntity.getName() + "'");
        }

        // TODO: check that relationship target is the same as <T> ??
        if (parentEntity.getRelationship(parent.getRelationship()).isToMany()) {
            return new ObjectRelator() {
                @Override
                public void relateToParent(DataObject object) {
                    parentObject.addToManyTarget(parent.getRelationship(), object, true);
                }
            };
        } else {
            return new ObjectRelator() {
                @Override
                public void relateToParent(DataObject object) {
                    parentObject.setToOneTarget(parent.getRelationship(), object, true);
                }
            };
        }
    }

    class ObjectRelator {

        void relateToParent(DataObject object) {
            // do nothing
        }

        void relate(AgRelationship agRelationship, DataObject object, DataObject relatedObject) {
            if (agRelationship.isToMany()) {
                object.addToManyTarget(agRelationship.getName(), relatedObject, true);
            } else {
                object.setToOneTarget(agRelationship.getName(), relatedObject, true);
            }
        }

        void unrelateAll(AgRelationship agRelationship, DataObject object) {
            unrelateAll(agRelationship, object, null);
        }

        void unrelateAll(AgRelationship agRelationship, DataObject object, RelationshipUpdate relationshipUpdate) {

            if (agRelationship.isToMany()) {

                @SuppressWarnings("unchecked")
                List<? extends DataObject> relatedObjects =
                        (List<? extends DataObject>) object.readProperty(agRelationship.getName());

                for (int i = 0; i < relatedObjects.size(); i++) {
                    DataObject relatedObject = relatedObjects.get(i);
                    if (relationshipUpdate == null || !relationshipUpdate.containsRelatedObject(relatedObject)) {
                        object.removeToManyTarget(agRelationship.getName(), relatedObject, true);
                        i--;
                    } else {
                        relationshipUpdate.removeUpdateForRelatedObject(relatedObject);
                    }
                }

            } else {
                object.setToOneTarget(agRelationship.getName(), null, true);
            }
        }
    }

    interface RelationshipUpdate {
        boolean containsRelatedObject(DataObject o);

        void removeUpdateForRelatedObject(DataObject o);
    }
}
