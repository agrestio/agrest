package io.agrest.jpa.pocessor.delete.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.EntityParent;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.query.JpaQueryBuilder;
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
    private final IJpaQueryAssembler queryAssembler;
//    private final IPathResolver pathResolver;

    public JpaDeleteMapChangesStage(@Inject AgDataMap dataMap,
                                    @Inject IJpaQueryAssembler queryAssembler) {
//            , @Inject IPathResolver pathResolver) {
        this.dataMap = dataMap;
        this.queryAssembler = queryAssembler;
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
        } else {
            // delete all !!
            return findAll(context);
        }
    }

    protected List<Object> findById(DeleteContext<Object> context) {

        List<Object> objects = new ArrayList<>(context.getIds().size());
        EntityManager entityManager = JpaDeleteStartStage.entityManager(context);
        for (AgObjectId id : context.getIds()) {
            // TODO: batch objects retrieval into a single query
            JpaQueryBuilder byIdQuery = queryAssembler.createByIdQuery(context.getAgEntity(), id.asMap(context.getAgEntity()));
            List<Object> result = byIdQuery.build(entityManager).getResultList();
            if (result.size() == 0) {
                // TODO: get proper entity name here?
                throw AgException.notFound("No object for ID '%s' and entity '%s'", id, context.getType().getSimpleName());
            }
            objects.add(result.get(0));
        }

        return objects;
    }

    @SuppressWarnings("unchecked")
    protected List<Object> findByParent(DeleteContext<Object> context, AgEntity<?> agParentEntity) {

        EntityParent<?> parent = context.getParent();
        EntityManager entityManager = JpaDeleteStartStage.entityManager(context);

        Map<String, Object> parentIdMap = parent.getId().asMap(context.getAgEntity());

        String relationship = parent.getRelationship();
        // TODO: compound id
        return JpaQueryBuilder.select("p." + relationship)
                .from(agParentEntity.getName() + " p")
                .where("p." + parentIdMap.keySet().iterator().next() + " = " + parentIdMap.values().iterator().next())
                .build(entityManager)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    protected List<Object> findAll(DeleteContext<Object> context) {
        EntityManager entityManager = JpaDeleteStartStage.entityManager(context);
        return JpaQueryBuilder.select("e")
                .from(context.getAgEntity().getName() + " e")
                .build(entityManager)
                .getResultList();
    }
}
