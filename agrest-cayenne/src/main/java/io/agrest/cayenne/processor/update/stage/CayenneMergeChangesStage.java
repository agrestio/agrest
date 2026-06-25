package io.agrest.cayenne.processor.update.stage;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.id.MultiValueId;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.EntityParent;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateMergeChangesStage;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectSelect;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A processor invoked for {@link io.agrest.UpdateStage#MERGE_CHANGES} stage.
 *
 * @since 2.7
 */
public class CayenneMergeChangesStage extends UpdateMergeChangesStage {

    private final EntityResolver entityResolver;
    private final IPathResolver pathResolver;

    public CayenneMergeChangesStage(
            @Inject ICayennePersister persister,
            @Inject IPathResolver pathResolver) {
        this.entityResolver = persister.entityResolver();
        this.pathResolver = pathResolver;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        merge((UpdateContext<Persistent>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T extends Persistent> void merge(UpdateContext<T> context) {
        Map<ChangeOperationType, List<ChangeOperation<T>>> ops = context.getChangeOperations();
        if (ops.isEmpty()) {
            return;
        }

        Consumer<Persistent> parentRelator = createParentRelator(context);
        for (ChangeOperation<T> op : ops.get(ChangeOperationType.CREATE)) {
            create(context, parentRelator, op.getUpdate());
        }

        for (ChangeOperation<T> op : ops.get(ChangeOperationType.UPDATE)) {
            update(parentRelator, op.getObject(), op.getUpdate());
        }

        for (ChangeOperation<T> op : ops.get(ChangeOperationType.DELETE)) {
            delete(op.getObject());
        }
    }

    protected <T extends Persistent> void delete(T o) {
        o.getObjectContext().deleteObject(o);
    }

    protected <T extends Persistent> void create(UpdateContext<T> context, Consumer<Persistent> parentRelator, EntityUpdate<T> update) {

        ObjectContext objectContext = CayenneUpdateStartStage.cayenneContext(context);
        T o = objectContext.newObject(context.getType());

        Map<String, Object> idParts = update.getIdParts();

        // set explicit ID
        if (!idParts.isEmpty()) {

            ObjEntity objEntity = objectContext.getEntityResolver().getObjEntity(context.getType());
            DbEntity dbEntity = objEntity.getDbEntity();
            AgEntity agEntity = context.getEntity().getAgEntity();

            Map<DbAttribute, Object> idByDbAttribute = mapToDbAttributes(agEntity, idParts);

            // need to make an additional check that the AgId is unique
            checkExisting(objectContext, agEntity, idByDbAttribute, idParts);

            if (isPrimaryKey(dbEntity, idByDbAttribute.keySet())) {
                createSingleFromPk(objEntity, idByDbAttribute, o);
            } else {
                createSingleFromIdValues(objEntity, idByDbAttribute, idParts, o);
            }
        }

        mergeChanges(update, o);
        parentRelator.accept(o);
    }

    protected <T extends Persistent> void update(Consumer<Persistent> parentRelator, T o, EntityUpdate<T> update) {
        mergeChanges(update, o);
        parentRelator.accept(o);
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

    private void createSingleFromPk(ObjEntity objEntity, Map<DbAttribute, Object> idByDbAttribute, Persistent o) {
        for (Map.Entry<DbAttribute, Object> e : idByDbAttribute.entrySet()) {
            setPrimaryKey(o, objEntity, e.getKey(), e.getValue());
        }
    }

    private <T extends Persistent> void checkExisting(
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
                    MultiValueId.mapToString(idByAgAttribute));
        }
    }

    private void createSingleFromIdValues(
            ObjEntity entity,
            Map<DbAttribute, Object> idByDbAttribute,
            Map<String, Object> idByAgAttribute,
            Persistent o) {

        for (Map.Entry<DbAttribute, Object> idPart : idByDbAttribute.entrySet()) {

            DbAttribute maybePk = idPart.getKey();
            if (maybePk == null) {
                throw AgException.badRequest("Can't create '%s' with id %s - not an ID DB attribute: %s",
                        entity.getName(),
                        MultiValueId.mapToString(idByAgAttribute),
                        idPart.getKey());
            }

            if (maybePk.isPrimaryKey()) {
                setPrimaryKey(o, entity, maybePk, idPart.getValue());
            } else {

                ObjAttribute objAttribute = entity.getAttributeForDbAttribute(maybePk);
                if (objAttribute == null) {
                    throw AgException.badRequest("Can't create '%s' with id %s - unknown object attribute: %s",
                            entity.getName(),
                            MultiValueId.mapToString(idByAgAttribute),
                            idPart.getKey());
                }

                o.writeProperty(objAttribute.getName(), idPart.getValue());
            }
        }
    }

    private void setPrimaryKey(Persistent o, ObjEntity entity, DbAttribute pk, Object idValue) {

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

    private <T extends Persistent> void mergeChanges(EntityUpdate<T> entityUpdate, T o) {

        // attributes
        for (Map.Entry<String, Object> e : entityUpdate.getAttributes().entrySet()) {
            o.writeProperty(e.getKey(), e.getValue());
        }

        // relationships
        ObjectContext context = o.getObjectContext();

        for (Map.Entry<String, Object> e : entityUpdate.getToOneIds().entrySet()) {

            String name = e.getKey();
            AgRelationship relationship = entityUpdate.getEntity().getRelationship(name);
            if (relationship == null) {
                continue;
            }

            Object relatedId = e.getValue();
            if (relatedId == null) {
                o.setToOneTarget(name, null, true);
                continue;
            }

            ObjectId relatedCayenneId = CayenneUtil.toObjectId(
                    pathResolver,
                    context,
                    relationship.getTargetEntity(),
                    relatedId);

            Persistent oldRelated = (Persistent) o.readProperty(name);

            // TODO: a bug (but mostly just dead code) - this check does not work, as we are comparing "relatedId"
            //  scalar with ObjectId, so it will return false no matter what
            if (oldRelated != null && oldRelated.getObjectId().equals(relatedId)) {
                continue;
            }

            // TODO: Note that "parent" (a special flavor of related object) is resolved via CayenneUtil. So
            //  here we should use CayenneUtil as well for consistency, and preferably batch-faulting related objects
            Persistent related = (Persistent) Cayenne.objectForPK(context, relatedCayenneId);
            if (related == null) {
                throw AgException.notFound("Related object '%s' with id of '%s' is not found",
                        relationship.getTargetEntity().getName(),
                        e.getValue());
            }

            o.setToOneTarget(name, related, true);
        }


        for (Map.Entry<String, Set<Object>> e : entityUpdate.getToManyIds().entrySet()) {

            String name = e.getKey();
            AgRelationship relationship = entityUpdate.getEntity().getRelationship(name);
            if (relationship == null) {
                continue;
            }

            // using set with predictable order that gives a predictable state of the final relationship list
            Set<ObjectId> relatedCayenneIds = new LinkedHashSet<>(e.getValue().size() * 2);
            for (Object id : e.getValue()) {
                if (id != null) {
                    relatedCayenneIds.add(CayenneUtil.toObjectId(
                            pathResolver,
                            context,
                            relationship.getTargetEntity(),
                            id));
                }
            }

            // unrelate objects no longer in relationship
            List<Persistent> relatedObjects = (List<Persistent>) o.readProperty(name);
            for (int i = 0; i < relatedObjects.size(); i++) {
                Persistent relatedObject = relatedObjects.get(i);
                if (!relatedCayenneIds.remove(relatedObject.getObjectId())) {
                    o.removeToManyTarget(relationship.getName(), relatedObject, true);
                    // a hack: we removed an object from relationship list, so need to reset the iteration index
                    i--;
                }
            }

            // link remaining added objects
            for (ObjectId id : relatedCayenneIds) {

                // TODO: Note that "parent" (a special flavor of related object) is resolved via CayenneUtil. So
                //  here we should use CayenneUtil as well for consistency, and preferably batch-faulting related objects
                Persistent related = (Persistent) Cayenne.objectForPK(context, id);

                if (related == null) {
                    throw AgException.notFound("Related object '%s' with id of '%s' is not found",
                            relationship.getTargetEntity().getName(),
                            e.getValue());
                }

                o.addToManyTarget(name, related, true);
            }
        }

        entityUpdate.setTargetObject(o);
    }

    protected Consumer<Persistent> createParentRelator(UpdateContext<? extends Persistent> context) {
        EntityParent<?> parent = context.getParent();
        if (parent == null) {
            return o -> {
            };
        }

        AgEntity<?> parentAgEntity = context.getSchema().getEntity(parent.getType());

        Persistent parentObject = findParent(context, parentAgEntity, parent);
        return parentAgEntity.getRelationship(parent.getRelationship()).isToMany()
                ? o -> parentObject.addToManyTarget(parent.getRelationship(), o, true)
                : o -> parentObject.setToOneTarget(parent.getRelationship(), o, true);
    }

    private Persistent findParent(UpdateContext<?> context, AgEntity<?> parentAgEntity, EntityParent<?> parent) {
        Persistent parentObject = (Persistent) CayenneUtil.findById(
                pathResolver,
                CayenneUpdateStartStage.cayenneContext(context),
                parentAgEntity,
                parent.getId());

        if (parentObject == null) {
            throw AgException.notFound("No parent object for ID '%s' and entity '%s'",
                    parent.getId(),
                    parentAgEntity.getName());
        }

        return parentObject;
    }

    protected DbAttribute dbAttributeForAgAttribute(AgEntity<?> agEntity, String attributeName) {
        ASTPath path = pathResolver.resolve(agEntity.getName(), attributeName).getPathExp();
        Object attribute = path.evaluate(entityResolver.getObjEntity(agEntity.getName()));
        return attribute instanceof ObjAttribute ? ((ObjAttribute) attribute).getDbAttribute() : (DbAttribute) attribute;
    }
}
