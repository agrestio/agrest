package io.agrest.cayenne.processor.update;

import io.agrest.AgObjectId;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.ResourceEntity;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjRelationship;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 2.7
 */
public class CayenneApplyServerParamsStage implements Processor<UpdateContext<?>> {

    private IEncoderService encoderService;
    private IConstraintsHandler constraintsHandler;
    private IMetadataService metadataService;
    private EntityResolver entityResolver;

    public CayenneApplyServerParamsStage(
            @Inject IEncoderService encoderService,
            @Inject IConstraintsHandler constraintsHandler,
            @Inject IMetadataService metadataService,
            @Inject ICayennePersister persister) {

        this.encoderService = encoderService;
        this.constraintsHandler = constraintsHandler;
        this.metadataService = metadataService;
        this.entityResolver = persister.entityResolver();
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        ResourceEntity<T> entity = context.getEntity();

        processPropagatedId(context);
        processExplicitId(context);
        processParentId(context);

        constraintsHandler.constrainUpdate(context, context.getWriteConstraints());

        // apply read constraints
        // TODO: should we only care about response constraints after the commit?
        constraintsHandler.constrainResponse(entity, null, context.getReadConstraints());

        if (context.getEncoder() == null) {
            // TODO: we don't need encoder if includeData=false... should we conditionally skip this step?
            // TODO: should we allow custom EntityFilters in update?
            context.setEncoder(encoderService.dataEncoder(entity));
        }
    }

    private <T> void processPropagatedId(UpdateContext<T> context) {

        if (context.getUpdates().isEmpty()) {
            return;
        }

        // for each relationship that propagates an id from the target entity to this, fill this entity "id" collection
        // with values from related PK...

        AgEntity<T> entity = context.getEntity().getAgEntity();

        // TODO: AgEntityOverlay relationships are ignored here
        for (AgRelationship r : entity.getRelationships()) {

            ObjRelationship objRelationship = objRelationshipForAgRelationship(entity.getName(), r);

            if(objRelationship != null) {
                List<DbRelationship> dbRelationships = objRelationship.getDbRelationships();
                if (dbRelationships.size() == 1) {

                    DbRelationship outgoingDbRelationship = dbRelationships.get(0);
                    DbRelationship incomingDbRelationship = outgoingDbRelationship.getReverseRelationship();

                    if (incomingDbRelationship.isToDependentPK()) {

                        List<DbJoin> joins = outgoingDbRelationship.getJoins();

                        // however unlikely, checking this for completeness
                        if (joins.size() == 0) {
                            throw new IllegalStateException("No joins for relationship " + r.getName());
                        } else if (joins.size() == 1) {
                            processSingleValuePropagatedId(context, r.getName(), joins.get(0));
                        } else {
                            processMapPropagatedId(context, r.getName(), joins);
                        }
                    }
                }
            }
        }
    }

    private void processSingleValuePropagatedId(UpdateContext<?> context, String agRelationshipName, DbJoin outgoingJoin) {

        for (EntityUpdate<?> u : context.getUpdates()) {
            // 'getSourceName' assumes AgEntity's id attribute name is based on DbAttribute name
            // TODO: check if PK is a map?
            Set<Object> pk = u.getRelatedIds().get(agRelationshipName);

            // if size != 1 : throw?
            if (pk != null && pk.size() == 1) {
                u.getOrCreateId().putIfAbsent(outgoingJoin.getSourceName(), pk.iterator().next());
            }
        }
    }

    private void processMapPropagatedId(UpdateContext<?> context, String agRelationshipName, List<DbJoin> outgoingJoins) {

        for (EntityUpdate<?> u : context.getUpdates()) {

            Set<Object> pk = u.getRelatedIds().get(agRelationshipName);

            // if size != 1 : throw?
            if (pk != null && pk.size() == 1) {
                // TODO: should we allow null Map though?
                if (!(pk instanceof Map)) {
                    throw new IllegalStateException("Expected more than one value in related 'id' for " + agRelationshipName);
                }

                // TODO: pretty sure this case has no unit tests
                Map<String, Object> pkMap = (Map) pk.iterator().next();
                for (DbJoin join : outgoingJoins) {
                    // 'getSourceName' and 'getTargetName' assumes AgEntity's id attribute name is based on DbAttribute name
                    u.getOrCreateId().putIfAbsent(join.getSourceName(), pkMap.get(join.getTargetName()));
                }
            }
        }
    }

    private <T> void processExplicitId(UpdateContext<T> context) {

        if (context.isById()) {

            // id was specified explicitly ... this means a few things:
            // * we expect zero or one object in the body
            // * if zero, create an empty update that will be attached to the ID.
            // * if more than one - throw...

            if (context.getUpdates().isEmpty()) {
                context.setUpdates(Collections.singleton(new EntityUpdate<>(context.getEntity().getAgEntity())));
            }

            AgEntity<T> entity = context.getEntity().getAgEntity();
            EntityUpdate<T> u = context.getFirst();
            u.getOrCreateId().putAll(context.getId().asMap(entity));
            u.setExplicitId();
        }
    }

    private <T> void processParentId(UpdateContext<T> context) {

        EntityParent<?> parent = context.getParent();

        if (parent != null && parent.getId() != null) {

            ObjRelationship fromParent = relationshipFromParent(context);
            if (fromParent != null) {

                // TODO: is this appropriate for flattened parent rels?
                DbRelationship incomingDbRelationship = fromParent.getDbRelationships().get(0);
                if (incomingDbRelationship.isToDependentPK()) {

                    AgObjectId id = parent.getId();
                    for (EntityUpdate<T> u : context.getUpdates()) {
                        for (DbJoin join : incomingDbRelationship.getJoins()) {
                            // 'getSourceName' and 'getTargetName' assumes AgEntity's id attribute name is based on DbAttribute name
                            u.getOrCreateId().putIfAbsent(join.getTargetName(), id.get(join.getSourceName()));
                        }
                    }
                }
            }
        }
    }

    private ObjRelationship relationshipFromParent(UpdateContext<?> context) {
        return context.getParent() != null
                ? entityResolver.getObjEntity(context.getParent().getType()).getRelationship(context.getParent().getRelationship())
                : null;
    }

    // TODO: copied verbatim from CayenneQueryAssembler... Unify this code?
    protected ObjRelationship objRelationshipForAgRelationship(String sourceEntityName, AgRelationship relationship) {
        return entityResolver.getObjEntity(sourceEntityName).getRelationship(relationship.getName());
    }

    // TODO: copied verbatim from CayenneQueryAssembler... Unify this code?
    protected Expression translateExpressionToSource(ObjRelationship relationship, Expression expression) {
        return expression != null
                ? relationship.getSourceEntity().translateToRelatedEntity(expression, relationship.getName())
                : null;
    }
}
