package io.agrest.jpa.pocessor.update.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.agrest.AgException;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.jpa.exp.IJpaExpParser;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.meta.AgDataMap;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaMapIdempotentFullSyncStage extends JpaMapIdempotentCreateOrUpdateStage {

    private final AgDataMap dataMap;
//    private final IPathResolver pathResolver;

    public JpaMapIdempotentFullSyncStage(
            @Inject AgDataMap dataMap,
//            @Inject IPathResolver pathResolver,
            @Inject IJpaExpParser qualifierParser,
            @Inject IJpaQueryAssembler queryAssembler,
            @Inject IAgJpaPersister persister) {

        super(qualifierParser, queryAssembler, persister);

        this.dataMap = dataMap;
//        this.pathResolver = pathResolver;
    }

    @Override
    protected <T> void collectUpdateDeleteOps(
            UpdateContext<T> context,
            ObjectMapper<T> mapper,
            UpdateMap<T> updateMap) {

        List<T> existing = existingObjects(context, updateMap.getIds(), mapper);
        if (existing.isEmpty()) {
            return;
        }

        List<ChangeOperation<T>> updateOps = new ArrayList<>(updateMap.getIds().size());
        List<ChangeOperation<T>> deleteOps = new ArrayList<>();

        for (T o : existing) {
            Object key = mapper.keyForObject(o);

            EntityUpdate<T> update = updateMap.remove(key);

            if (update == null) {
                deleteOps.add(new ChangeOperation<>(ChangeOperationType.DELETE, context.getEntity().getAgEntity(), o, null));
            } else {
                updateOps.add(new ChangeOperation<>(ChangeOperationType.UPDATE, update.getEntity(), o, update));
            }
        }

        context.setChangeOperations(ChangeOperationType.UPDATE, updateOps);
        context.setChangeOperations(ChangeOperationType.DELETE, deleteOps);
    }

    @Override
    protected <T> List<T> existingObjects(
            UpdateContext<T> context,
            Collection<Object> keys,
            ObjectMapper<T> mapper) {

        if(context.getParent() != null) {
            throw AgException.internalServerError("No implemented yet.");
        }

        return Collections.emptyList();

//        EntityParent<?> parent = context.getParent();
//        Expression rootQualifier = parent != null ?
//                CayenneUtil.parentQualifier(pathResolver, dataMap.getEntity(parent.getType()), parent, entityResolver)
//                : null;
//
//        buildRootQuery(context.getEntity(), rootQualifier);
//
//         TODO: implement entity-tied resolvers for updates to avoid duplicating selecting logic
//
//        List<T> objects = fetchRootEntity(CayenneUpdateStartStage.cayenneContext(context), context.getEntity());
//
//        if (context.isById() && objects.size() > 1) {
//            throw AgException.internalServerError(
//                    "Found more than one object for ID '%s' and entity '%s'",
//                    context.getId(),
//                    context.getEntity().getName());
//        }
//        return objects;
    }
}
