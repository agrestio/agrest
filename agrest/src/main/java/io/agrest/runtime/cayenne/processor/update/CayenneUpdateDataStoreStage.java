package io.agrest.runtime.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.cayenne.CayenneAgAttribute;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.cayenne.processor.Util;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.Fault;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.reflect.ClassDescriptor;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 2.7
 */
public abstract class CayenneUpdateDataStoreStage implements Processor<UpdateContext<?>> {

    private IMetadataService metadataService;

    public CayenneUpdateDataStoreStage(IMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        sync((UpdateContext<DataObject>) context);
        CayenneUpdateStartStage.cayenneContext(context).commitChanges();

        // Stores parent-child result list in ResourceEntity
        // TODO Replace this by dedicated select child stage during of update stages refactoring
        ResourceEntity entity = context.getEntity();
        Map<String, ResourceEntity<?>> children = entity.getChildren();
        List rootResult = new ArrayList();
        for (EntityUpdate<?> u : context.getUpdates()) {
            DataObject o = (DataObject) u.getMergedTo();
            //saves root elements
            rootResult.add(o);
            //assigns children
            assignChildrenToParent(o, entity, children);
        }
        entity.setResult(rootResult);

        return ProcessorOutcome.CONTINUE;
    }

    protected void assignChildrenToParent(DataObject root, ResourceEntity<?> parent, Map<String, ResourceEntity<?>> children) {
        if (!children.isEmpty()) {
            for (Map.Entry<String, ResourceEntity<?>> e : children.entrySet()) {
                ResourceEntity childEntity = e.getValue();

                Object result = root.readPropertyDirectly(e.getKey());
                if (result == null || result instanceof Fault) {
                    continue;
                }
                AgObjectId id = root.getObjectId().getIdSnapshot().size() > 1
                        ? new CompoundObjectId(root.getObjectId().getIdSnapshot())
                        : new SimpleObjectId(root.getObjectId().getIdSnapshot().values().iterator().next());

                AgRelationship rel = parent.getChild(e.getKey()).getIncoming();
                if (rel.isToMany() && result instanceof List) {
                    List r = (List) result;

                    childEntity.setToManyResult(id, r);
                    for (Object ro : r) {
                        assignChildrenToParent((DataObject) ro, childEntity, childEntity.getChildren());
                    }

                } else {
                    childEntity.setToOneResult(id, result);
                    assignChildrenToParent((DataObject) result, childEntity, childEntity.getChildren());
                }
            }
        }
    }

    protected abstract <T extends DataObject> void sync(UpdateContext<T> context);

    protected <T extends DataObject> void create(UpdateContext<T> context) {

        ObjectRelator relator = createRelator(context);

        for (EntityUpdate<T> u : context.getUpdates()) {
            createSingle(context, relator, u);
        }
    }

    protected <T extends DataObject> void updateSingle(UpdateContext<T> context, T o, Collection<EntityUpdate<T>> updates) {

        ObjectRelator relator = createRelator(context);

        for (EntityUpdate<T> u : updates) {
            mergeChanges(u, o, relator);
        }

        relator.relateToParent(o);
    }

    protected <T extends DataObject> void createSingle(UpdateContext<T> context, ObjectRelator relator, EntityUpdate<T> u) {

        ObjectContext objectContext = CayenneUpdateStartStage.cayenneContext(context);
        DataObject o = objectContext.newObject(context.getType());
        Map<String, Object> idByAgAttribute = u.getId();

        // set explicit ID
        if (idByAgAttribute != null) {

            if (context.isIdUpdatesDisallowed() && u.isExplicitId()) {
                throw new AgException(Response.Status.BAD_REQUEST, "Setting ID explicitly is not allowed: " + idByAgAttribute);
            }

            ObjEntity objEntity = objectContext.getEntityResolver().getObjEntity(context.getType());
            DbEntity dbEntity = objEntity.getDbEntity();
            AgEntity agEntity = context.getEntity().getAgEntity();

            Map<DbAttribute, Object> idByDbAttribute = mapToDbAttributes(agEntity, idByAgAttribute);

            if (isPrimaryKey(dbEntity, idByDbAttribute.keySet())) {
                createSingleFromPk(objEntity, idByDbAttribute, o);
            } else {
                // need to make an additional check that the AgId is unique
                checkExisting(objectContext, agEntity, idByDbAttribute, idByAgAttribute);
                createSingleFromIdValues(objEntity, idByDbAttribute, idByAgAttribute, o);
            }
        }

        mergeChanges(u, o, relator);
        relator.relateToParent(o);
    }

    // translate "id" expressed in terms on public Ag names to Cayenne DbAttributes
    private Map<DbAttribute, Object> mapToDbAttributes(AgEntity<?> agEntity, Map<String, Object> idByAgAttribute) {

        Map<DbAttribute, Object> idByDbAttribute = new HashMap<>((int) (idByAgAttribute.size() / 0.75) + 1);
        for (Map.Entry<String, Object> e : idByAgAttribute.entrySet()) {

            AgAttribute agAttribute = agEntity.getIdAttribute(e.getKey());
            if (agAttribute == null) {
                agAttribute = agEntity.getAttribute(e.getKey());
            }

            if (agAttribute == null) {
                throw new AgException(Response.Status.BAD_REQUEST, "Invalid attribute '"
                        + agEntity.getName()
                        + "."
                        + e.getKey()
                        + "'");
            }

            // I guess this kind of type checking is not too dirty ... CayenneAgDbAttribute was created by Cayenne
            // part of Ag, and we are back again in Cayenne part of Ag, trying to map Ag model back to Cayenne
            DbAttribute dbAttribute;

            if (agAttribute instanceof CayenneAgAttribute) {
                dbAttribute = ((CayenneAgAttribute) agAttribute).getDbAttribute();
            } else {
                throw new AgException(Response.Status.BAD_REQUEST, "Not a mapped persistent attribute '"
                        + agEntity.getName()
                        + "."
                        + e.getKey()
                        + "'");
            }

            idByDbAttribute.put(dbAttribute, e.getValue());
        }

        return idByDbAttribute;
    }

    private void createSingleFromPk(ObjEntity objEntity, Map<DbAttribute, Object> idByDbAttribute, DataObject o) {
        for (Map.Entry<DbAttribute, Object> e : idByDbAttribute.entrySet()) {
            setPrimaryKey(o, objEntity, e.getKey(), e.getValue());
        }
    }

    private <T extends DataObject> void checkExisting(
            ObjectContext objectContext,
            AgEntity<T> agEntity,
            Map<DbAttribute, Object> idByDbAttribute,
            Map<String, Object> idByAgAttribute) {

        ObjectSelect<DataRow> query = ObjectSelect.dataRowQuery(agEntity.getType());
        for (Map.Entry<DbAttribute, Object> e : idByDbAttribute.entrySet()) {
            query.and(ExpressionFactory.matchDbExp(e.getKey().getName(), e.getValue()));
        }

        if (query.selectOne(objectContext) != null) {
            throw new AgException(Response.Status.BAD_REQUEST, "Can't create '"
                    + agEntity.getName()
                    + "' with id "
                    + CompoundObjectId.mapToString(idByAgAttribute)
                    + " - already exists");
        }
    }

    private void createSingleFromIdValues(
            ObjEntity entity,
            Map<DbAttribute, Object> idByDbAttribute,
            Map<String, Object> idByAgAttribute,
            DataObject o) {

        for (Map.Entry<DbAttribute, Object> idPart : idByDbAttribute.entrySet()) {

            DbAttribute maybePk = idPart.getKey();
            if (maybePk == null) {
                throw new AgException(Response.Status.BAD_REQUEST, "Can't create '"
                        + entity.getName()
                        + "' with id "
                        + CompoundObjectId.mapToString(idByAgAttribute)
                        + " - not an ID DB attribute: "
                        + idPart.getKey());
            }

            if (maybePk.isPrimaryKey()) {
                setPrimaryKey(o, entity, maybePk, idPart.getValue());
            } else {

                ObjAttribute objAttribute = entity.getAttributeForDbAttribute(maybePk);
                if (objAttribute == null) {
                    throw new AgException(Response.Status.BAD_REQUEST, "Can't create '"
                            + entity.getName()
                            + "' with id " + CompoundObjectId.mapToString(idByAgAttribute)
                            + " - unknown object attribute: "
                            + idPart.getKey());
                }

                o.writeProperty(objAttribute.getName(), idPart.getValue());
            }
        }
    }

    private void setPrimaryKey(DataObject o, ObjEntity entity, DbAttribute pk, Object idValue) {

        // 1. meaningful ID
        // TODO: must precompile all this... figuring this on the fly is slow
        ObjAttribute opk = entity.getAttributeForDbAttribute(pk);
        if (opk != null) {
            o.writeProperty(opk.getName(), idValue);
        }
        // 2. PK is auto-generated ... I guess this is sorta expected to fail - generated meaningless PK should not be
        // pushed from the client
        else if (pk.isGenerated()) {
            throw new AgException(Response.Status.BAD_REQUEST, "Can't create '" + entity.getName()
                    + "' with fixed id");
        }
        // 3. probably a propagated ID.
        else {
            o.getObjectId().getReplacementIdMap().put(pk.getName(), idValue);
        }
    }

    /**
     * @return true if all PK columns are represented in {@code keys}
     */
    private boolean isPrimaryKey(DbEntity entity, Collection<DbAttribute> maybePk) {
        int pkSize = entity.getPrimaryKeys().size();
        if (pkSize > maybePk.size()) {
            return false;
        }

        int countPk = 0;
        for (DbAttribute a : maybePk) {
            if (a.isPrimaryKey()) {
                countPk++;
            }
        }

        return countPk >= pkSize;
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

    protected <T extends DataObject> ObjectRelator createRelator(UpdateContext<T> context) {

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

    interface RelationshipUpdate {
        boolean containsRelatedObject(DataObject o);

        void removeUpdateForRelatedObject(DataObject o);
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
}
