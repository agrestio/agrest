package io.agrest.cayenne.processor.delete.stage;

import io.agrest.AgException;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.meta.AgEntity;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.EntityParent;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.stage.DeleteMapChangesStage;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectSelect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A processor for the {@link io.agrest.DeleteStage#MAP_CHANGES} stage that associates persistent objects with delete
 * operations.
 *
 * @since 4.8
 */
public class CayenneDeleteMapChangesStage extends DeleteMapChangesStage {

    private final IPathResolver pathResolver;
    private final ICayenneQueryAssembler queryAssembler;

    public CayenneDeleteMapChangesStage(
            @Inject IPathResolver pathResolver,
            @Inject ICayenneQueryAssembler queryAssembler) {
        this.pathResolver = pathResolver;
        this.queryAssembler = queryAssembler;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        mapDeleteOperations((DeleteContext<Persistent>) context);
        return context.getDeleteOperations().isEmpty()
                ? ProcessorOutcome.STOP
                : ProcessorOutcome.CONTINUE;
    }

    protected <T extends Persistent> void mapDeleteOperations(DeleteContext<T> context) {
        AgEntity<T> agEntity = context.getAgEntity();
        List<T> objects = findObjectsToDelete(context);
        List<ChangeOperation<T>> ops = new ArrayList<>(objects.size());

        for (T o : objects) {
            ops.add(new ChangeOperation<>(ChangeOperationType.DELETE, agEntity, o, null));
        }

        context.setDeleteOperations(ops);
    }

    protected <T extends Persistent> List<T> findObjectsToDelete(DeleteContext<T> context) {

        if (context.isByIds()) {
            return findByIds(context);
        } else if (context.getParent() != null) {
            return findByParent(context, context.getParent());
        }
        // delete all !!
        else {
            return findAll(context);
        }
    }

    protected <T extends Persistent> List<T> findByIds(DeleteContext<T> context) {

        ObjectContext cayenneContext = CayenneDeleteStartStage.cayenneContext(context);
        List<T> objects = queryAssembler.createQueryForIds(context.getAgEntity(), context.getIds()).select(cayenneContext);

        // DELETE is idempotent, so if some objects are missing, we should proceed ...
        // Also, only return 404 if zero objects matched

        if (objects.isEmpty()) {
            ObjEntity entity = cayenneContext.getEntityResolver().getObjEntity(context.getType());

            String idsString = context.getIds().stream().map(id -> id.toString()).collect(Collectors.joining(","));
            throw AgException.notFound("No matching objects for entity '%s' and ids: %s",
                    entity.getName(), idsString);
        }

        return objects;
    }

    protected <T extends Persistent> List<T> findByParent(DeleteContext<T> context, EntityParent<?> parent) {

        AgEntity<?> parentAgEntity = context.getSchema().getEntity(parent.getType());
        ObjectContext cayenneContext = CayenneDeleteStartStage.cayenneContext(context);
        Object parentObject = CayenneUtil.findById(pathResolver, cayenneContext, parentAgEntity, parent.getId());

        if (parentObject == null) {
            // TODO: resolve  entity by name, not type?
            ObjEntity entity = cayenneContext.getEntityResolver().getObjEntity(parent.getType());
            throw AgException.notFound("No parent object for ID '%s' and entity '%s'", parent.getId(), entity.getName());
        }

        return ObjectSelect.query(context.getType())
                .where(CayenneUtil.parentQualifier(pathResolver, context.getSchema().getEntity(parent.getType()), parent, cayenneContext.getEntityResolver()))
                .select(CayenneDeleteStartStage.cayenneContext(context));
    }

    protected <T extends Persistent> List<T> findAll(DeleteContext<T> context) {
        return ObjectSelect.query(context.getType()).select(CayenneDeleteStartStage.cayenneContext(context));
    }
}
