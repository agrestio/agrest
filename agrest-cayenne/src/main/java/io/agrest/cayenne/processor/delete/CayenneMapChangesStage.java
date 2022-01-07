package io.agrest.cayenne.processor.delete;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.EntityParent;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectSelect;

import java.util.ArrayList;
import java.util.List;

/**
 * A processor for the {@link io.agrest.DeleteStage#MAP_CHANGES} stage that associates persistent objects with delete
 * operations.
 *
 * @since 4.8
 */
public class CayenneMapChangesStage implements Processor<DeleteContext<?>> {

    private final AgDataMap dataMap;
    private final IPathResolver pathResolver;

    public CayenneMapChangesStage(@Inject AgDataMap dataMap, @Inject IPathResolver pathResolver) {
        this.dataMap = dataMap;
        this.pathResolver = pathResolver;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        mapDeleteOperations((DeleteContext<DataObject>) context);
        return context.getDeleteOperations().isEmpty()
                ? ProcessorOutcome.STOP
                : ProcessorOutcome.CONTINUE;
    }

    protected <T extends DataObject> void mapDeleteOperations(DeleteContext<T> context) {
        AgEntity<T> agEntity = context.getAgEntity();
        List<T> objects = findObjectsToDelete(context);
        List<ChangeOperation<T>> ops = new ArrayList<>(objects.size());

        for (T o : objects) {
            ops.add(new ChangeOperation<>(ChangeOperationType.DELETE, agEntity, o, null));
        }

        context.setDeleteOperations(ops);
    }

    protected <T extends DataObject> List<T> findObjectsToDelete(DeleteContext<T> context) {

        if (context.isById()) {
            return findById(context);
        } else if (context.getParent() != null) {
            return findByParent(context, dataMap.getEntity(context.getParent().getType()));
        }
        // delete all !!
        else {
            return findAll(context);
        }
    }

    protected <T extends DataObject> List<T> findById(DeleteContext<T> context) {

        List<T> objects = new ArrayList<>(context.getIds().size());
        ObjectContext cayenneContext = CayenneDeleteStartStage.cayenneContext(context);

        for (AgObjectId id : context.getIds()) {

            // TODO: batch objects retrieval into a single query

            T o = CayenneUtil.findById(pathResolver, cayenneContext, context.getType(), context.getAgEntity(), id);

            if (o == null) {
                ObjEntity entity = cayenneContext.getEntityResolver().getObjEntity(context.getType());
                throw AgException.notFound("No object for ID '%s' and entity '%s'", id, entity.getName());
            }

            objects.add(o);
        }

        return objects;
    }

    protected <T extends DataObject> List<T> findByParent(DeleteContext<T> context, AgEntity<?> agParentEntity) {

        EntityParent<?> parent = context.getParent();
        ObjectContext cayenneContext = CayenneDeleteStartStage.cayenneContext(context);
        Object parentObject = CayenneUtil.findById(pathResolver, cayenneContext, parent.getType(), agParentEntity, parent.getId());

        if (parentObject == null) {
            ObjEntity entity = cayenneContext.getEntityResolver().getObjEntity(parent.getType());
            throw AgException.notFound("No parent object for ID '%s' and entity '%s'", parent.getId(), entity.getName());
        }

        return ObjectSelect.query(context.getType())
                .where(CayenneUtil.parentQualifier(pathResolver, parent, cayenneContext.getEntityResolver()))
                .select(CayenneDeleteStartStage.cayenneContext(context));
    }

    protected <T extends DataObject> List<T> findAll(DeleteContext<T> context) {
        return ObjectSelect.query(context.getType()).select(CayenneDeleteStartStage.cayenneContext(context));
    }
}
