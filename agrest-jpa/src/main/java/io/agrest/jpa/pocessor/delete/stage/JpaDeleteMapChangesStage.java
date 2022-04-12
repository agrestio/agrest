package io.agrest.jpa.pocessor.delete.stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.EntityParent;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.stage.DeleteMapChangesStage;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import jakarta.persistence.EntityManager;
import org.apache.cayenne.di.Inject;

/**
 * A processor for the {@link io.agrest.DeleteStage#MAP_CHANGES} stage that associates persistent objects with delete
 * operations.
 *
 * @since 5.0
 */
public class JpaDeleteMapChangesStage extends DeleteMapChangesStage {

    private final AgDataMap dataMap;
//    private final IPathResolver pathResolver;

    public JpaDeleteMapChangesStage(@Inject AgDataMap dataMap) {
//            , @Inject IPathResolver pathResolver) {
        this.dataMap = dataMap;
//        this.pathResolver = pathResolver;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        mapDeleteOperations((DeleteContext<Object>)context);
        return context.getDeleteOperations().isEmpty()
                ? ProcessorOutcome.STOP
                : ProcessorOutcome.CONTINUE;
    }

    protected void mapDeleteOperations(DeleteContext<Object> context) {
        AgEntity<Object> agEntity = context.getAgEntity();
        List<Object> objects = findObjectsToDelete(context);
        List<ChangeOperation<Object>> ops = new ArrayList<>(objects.size());

        for (Object o : objects) {
            ops.add(new ChangeOperation<>(ChangeOperationType.DELETE, agEntity, o, null));
        }

        context.setDeleteOperations(ops);
    }

    protected List<Object> findObjectsToDelete(DeleteContext<Object> context) {

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

    protected List<Object> findById(DeleteContext<Object> context) {

        List<Object> objects = new ArrayList<>(context.getIds().size());
        EntityManager entityManager = JpaDeleteStartStage.entityManager(context);
        for (AgObjectId id : context.getIds()) {

            // TODO: batch objects retrieval into a single query
            Object o = entityManager.find(context.getType(), id);
            if (o == null) {
                // TODO: get proper entity name here?
                throw AgException.notFound("No object for ID '%s' and entity '%s'", id, context.getType().getSimpleName());
            }

            objects.add(o);
        }

        return objects;
    }

    protected List<Object> findByParent(DeleteContext<Object> context, AgEntity<?> agParentEntity) {

        EntityParent<?> parent = context.getParent();
        EntityManager entityManager = JpaDeleteStartStage.entityManager(context);

        Object parentObject = entityManager.find(parent.getType(), parent.getId());

        if (parentObject == null) {
            throw AgException.notFound("No parent object for ID '%s' and entity '%s'",
                    parent.getId(), parent.getType().getSimpleName());
        }

        return Collections.emptyList();
        // TODO: get objects by parent
//        return ObjectSelect.query(context.getType())
//                .where(CayenneUtil.parentQualifier(pathResolver, agParentEntity, parent, cayenneContext.getEntityResolver()))
//                .select(CayenneDeleteStartStage.cayenneContext(context));
    }

    protected List<Object> findAll(DeleteContext<Object> context) {
        return JpaDeleteStartStage.entityManager(context)
                .createQuery("select e from " + context.getAgEntity().getName() + " e", context.getType())
                .getResultList();
    }
}
