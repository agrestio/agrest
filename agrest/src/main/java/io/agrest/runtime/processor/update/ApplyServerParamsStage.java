package io.agrest.runtime.processor.update;

import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgPersistentRelationship;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.meta.IMetadataService;
import org.apache.cayenne.di.Inject;

import java.util.Collections;
import java.util.Map;

/**
 * @since 2.7
 */
public class ApplyServerParamsStage implements Processor<UpdateContext<?>> {

    private IEncoderService encoderService;
    private IConstraintsHandler constraintsHandler;
    private IMetadataService metadataService;

    public ApplyServerParamsStage(
            @Inject IEncoderService encoderService,
            @Inject IConstraintsHandler constraintsHandler,
            @Inject IMetadataService metadataService) {

        this.encoderService = encoderService;
        this.constraintsHandler = constraintsHandler;
        this.metadataService = metadataService;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        ResourceEntity<T> entity = context.getEntity();

        processExplicitId(context);
        processParentId(context);

        constraintsHandler.constrainUpdate(context, context.getWriteConstraints());

        // apply read constraints (TODO: should we only care about response
        // constraints after the commit?)
        constraintsHandler.constrainResponse(entity, null, context.getReadConstraints());

        if(context.getEncoder() == null) {
            // TODO: we don't need encoder if includeData=false... should we  conditionally skip this step?
            // TODO: should we allow custom EntityFilters in update?
            context.setEncoder(encoderService.dataEncoder(entity));
        }
    }

    private <T> void processExplicitId(UpdateContext<T> context) {

        if (context.isById()) {

            // id was specified explicitly ... this means a few things:
            // * we expect zero or one object in the body
            // * if zero, create an empty update that will be attached to the
            // ID.
            // * if more than one - throw...

            if (context.getUpdates().isEmpty()) {
                context.setUpdates(Collections.singleton(new EntityUpdate<>(context.getEntity().getAgEntity())));
            }

            AgEntity<T> entity = context.getEntity().getAgEntity();
            EntityUpdate<T> u = context.getFirst();
            Map<String, Object> idMap = u.getOrCreateId();
            idMap.putAll(context.getId().asMap(entity));

            u.setExplicitId();
        }
    }

    private <T> void processParentId(UpdateContext<T> context) {

        EntityParent<?> parent = context.getParent();

        if (parent != null && parent.getId() != null) {
            AgRelationship fromParent = relationshipFromParent(context);
            if (fromParent instanceof AgPersistentRelationship) {
                AgPersistentRelationship r = (AgPersistentRelationship) fromParent;
                if (r.isToDependentEntity()) {
                    for (EntityUpdate<T> u : context.getUpdates()) {
                        u.getOrCreateId().putAll(r.extractId(parent.getId()));
                    }
                }
            }
        }
    }

    private AgRelationship relationshipFromParent(UpdateContext<?> context) {

        EntityParent<?> parent = context.getParent();

        if (parent == null) {
            return null;
        }

        AgRelationship r = metadataService.getAgRelationship(parent);
        if (r instanceof AgPersistentRelationship) {
            return r;
        }

        return null;
    }
}
