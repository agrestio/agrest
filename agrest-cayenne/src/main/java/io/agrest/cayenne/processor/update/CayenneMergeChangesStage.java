package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.CompoundObjectId;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.reflect.ClassDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A processor invoked for {@link io.agrest.UpdateStage#MERGE_CHANGES} stage.
 *
 * @since 2.7
 */
public class CayenneMergeChangesStage implements Processor<UpdateContext<?>> {

    private final AgDataMap dataMap;
    private final EntityResolver entityResolver;
    private final IPathResolver pathResolver;

    public CayenneMergeChangesStage(
            @Inject AgDataMap dataMap,
            @Inject ICayennePersister persister,
            @Inject IPathResolver pathResolver) {
        this.dataMap = dataMap;
        this.entityResolver = persister.entityResolver();
        this.pathResolver = pathResolver;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        merge((UpdateContext<DataObject>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T extends DataObject> void merge(UpdateContext<T> context) {
        Map<ChangeOperationType, List<ChangeOperation<T>>> ops = context.getChangeOperations();
        if (ops.isEmpty()) {
            return;
        }

        ObjectRelator relator = createRelator(context);
        for (ChangeOperation<T> op : ops.get(ChangeOperationType.CREATE)) {
            create(context, relator, op.getUpdate());
        }

        for (ChangeOperation<T> op : ops.get(ChangeOperationType.UPDATE)) {
            update(relator, op.getObject(), op.getUpdate());
        }

        for (ChangeOperation<T> op : ops.get(ChangeOperationType.DELETE)) {
            delete(op.getObject());
        }
    }

    protected <T extends DataObject> void delete(T o) {
        o.getObjectContext().deleteObject(o);
    }

    protected <T extends DataObject> void create(UpdateContext<T> context, ObjectRelator relator, EntityUpdate<T> update) {

        ObjectContext objectContext = CayenneUpdateStartStage.cayenneContext(context);
        DataObject o = objectContext.newObject(context.getType());


        Map<String, Object> idByAgAttribute = update.getId();

        // set explicit ID
        if (idByAgAttribute != null) {

            if (context.isIdUpdatesDisallowed() && update.isExplicitId()) {
                throw AgException.badRequest("Setting ID explicitly is not allowed: %s", idByAgAttribute);
            }

            ObjEntity objEntity = objectContext.getEntityResolver().getObjEntity(context.getType());
            DbEntity dbEntity = objEntity.getDbEntity();
            AgEntity agEntity = context.getEntity().getAgEntity();

            Map<DbAttribute, Object> idByDbAttribute = mapToDbAttributes(agEntity, idByAgAttribute);

            // need to make an additional check that the AgId is unique
            checkExisting(objectContext, agEntity, idByDbAttribute, idByAgAttribute);

            if (isPrimaryKey(dbEntity, idByDbAttribute.keySet())) {
                createSingleFromPk(objEntity, idByDbAttribute, o);
            } else {
                createSingleFromIdValues(objEntity, idByDbAttribute, idByAgAttribute, o);
            }
        }

        mergeChanges(update, o, relator);

        relator.relateToParent(o);
    }

    protected <T extends DataObject> void update(ObjectRelator relator, T o, EntityUpdate<T> update) {
        mergeChanges(update, o, relator);
        relator.relateToParent(o);
    }

    // translate "id" expressed in terms on public Ag names to Cayenne DbAttributes
    private Map<DbAttribute, Object> mapToDbAttributes(AgEntity<?> agEntity, Map<String, Object> idByAgAttribute) {

        Map<DbAttribute, Object> idByDbAttribute = new HashMap<>((int) (idByAgAttribute.size() / 0.75) + 1);
        for (Map.Entry<String, Object> e : idByAgAttribute.entrySet()) {

            DbAttribute dbAttribute = dbAttributeForAgAttribute(agEntity, e.getKey());

            if (dbAttribute == null) {
                throw AgException.badRequest("Not a mapped persistent attribute '%s.%s'", agEntity.getName(), e.getKey());
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
            throw AgException.badRequest("Can't create '%s' with id %s - already exists",
                    agEntity.getName(),
                    CompoundObjectId.mapToString(idByAgAttribute));
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
                throw AgException.badRequest("Can't create '%s' with id %s - not an ID DB attribute: %s",
                        entity.getName(),
                        CompoundObjectId.mapToString(idByAgAttribute),
                        idPart.getKey());
            }

            if (maybePk.isPrimaryKey()) {
                setPrimaryKey(o, entity, maybePk, idPart.getValue());
            } else {

                ObjAttribute objAttribute = entity.getAttributeForDbAttribute(maybePk);
                if (objAttribute == null) {
                    throw AgException.badRequest("Can't create '%s' with id %s - unknown object attribute: %s",
                            entity.getName(),
                            CompoundObjectId.mapToString(idByAgAttribute),
                            idPart.getKey());
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
            throw AgException.badRequest("Can't create '%s' with fixed id", entity.getName());
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
                throw AgException.badRequest(
                        "Relationship is to-one, but received update with multiple objects: %s",
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
                    throw AgException.notFound("Related object '%s' with ID '%s' is not found",
                            relationship.getTargetEntityName(),
                            e.getValue());
                }

                relator.relate(agRelationship, o, related);
            }
        }

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
        AgEntity<?> parentAgEntity = dataMap.getEntity(context.getParent().getType());
        final DataObject parentObject = (DataObject) CayenneUtil.findById(
                pathResolver,
                objectContext,
                parent.getType(),
                parentAgEntity,
                parent.getId().get());

        if (parentObject == null) {
            throw AgException.notFound("No parent object for ID '%s' and entity '%s'", parent.getId(), parentEntity.getName());
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

    protected DbAttribute dbAttributeForAgAttribute(AgEntity<?> agEntity, String attributeName) {
        ASTPath path = pathResolver.resolve(agEntity, attributeName).getPathExp();
        Object attribute = path.evaluate(entityResolver.getObjEntity(agEntity.getName()));
        return attribute instanceof ObjAttribute ? ((ObjAttribute) attribute).getDbAttribute() : (DbAttribute) attribute;
    }

    interface RelationshipUpdate {
        boolean containsRelatedObject(DataObject o);

        void removeUpdateForRelatedObject(DataObject o);
    }

    static class ObjectRelator {

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
