package io.agrest.jpa.pocessor.update.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.agrest.AgException;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.NestedResourceEntity;
import io.agrest.ObjectMapper;
import io.agrest.jpa.exp.IJpaExpParser;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.jpa.query.JpaQueryBuilder;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaMapIdempotentFullSyncStage extends JpaMapIdempotentCreateOrUpdateStage {

    private final AgDataMap dataMap;

    public JpaMapIdempotentFullSyncStage(
            @Inject AgDataMap dataMap,
            @Inject IJpaExpParser qualifierParser,
            @Inject IJpaQueryAssembler queryAssembler,
            @Inject IAgJpaPersister persister) {

        super(qualifierParser, queryAssembler, persister);
        this.dataMap = dataMap;
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

        buildRootQuery(context);
        List<T> objects = fetchRootEntity(context);

        if (context.isById() && objects.size() > 1) {
            throw AgException.internalServerError(
                    "Found more than one object for ID '%s' and entity '%s'",
                    context.getId(),
                    context.getEntity().getName());
        }

        return objects;
    }

    protected <T> void buildRootQuery(UpdateContext<T> context) {
        // 1. build root query + nested queries
        JpaQueryBuilder rootQuery;
        EntityParent<?> parent = context.getParent();
        if(parent != null) {
            AgEntity<?> agEntity = dataMap.getEntity(parent.getType());
            AgRelationship incomingRelationship = agEntity.getRelationship(parent.getRelationship());
            if(incomingRelationship == null) {
                throw AgException.internalServerError("Invalid parent relationship: '%s'", parent.getRelationship());
            }
            if (incomingRelationship.isToMany()) {
                rootQuery = JpaQueryBuilder.select("r")
                        .from(agEntity.getName() + " e")
                        .from(", IN (e." + parent.getRelationship() + ") r");
            } else {
                rootQuery = JpaQueryBuilder.select("e." + parent.getRelationship())
                        .from(agEntity.getName() + " e");
            }
        } else {
            rootQuery = JpaQueryBuilder.select("e").from(context.getEntity().getName() + " e");
        }

        JpaProcessor.getRootEntity(context.getEntity()).setSelect(rootQuery);

        for (Map.Entry<String, NestedResourceEntity<?>> e : context.getEntity().getChildren().entrySet()) {
            buildNestedQuery(e.getValue());
        }
    }
}
