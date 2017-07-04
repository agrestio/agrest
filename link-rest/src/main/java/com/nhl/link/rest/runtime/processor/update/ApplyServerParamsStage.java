package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentRelationship;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.processor2.ProcessorOutcome;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
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

        // TODO: we don't need encoder if includeData=false... should we
        // conditionally skip this step?
        context.setEncoder(encoderService.dataEncoder(entity));
    }

    private <T> void processExplicitId(UpdateContext<T> context) {

        if (context.isById()) {

            // id was specified explicitly ... this means a few things:
            // * we expect zero or one object in the body
            // * if zero, create an empty update that will be attached to the
            // ID.
            // * if more than one - throw...

            if (context.getUpdates().isEmpty()) {
                context.setUpdates(Collections.singleton(new EntityUpdate<>(context.getEntity().getLrEntity())));
            }

            LrEntity<T> entity = context.getEntity().getLrEntity();
            EntityUpdate<T> u = context.getFirst();
            Map<String, Object> idMap = u.getOrCreateId();
            idMap.putAll(context.getId().asMap(entity));

            u.setExplicitId();
        }
    }

    private <T> void processParentId(UpdateContext<T> context) {

        EntityParent<?> parent = context.getParent();

        if (parent != null && parent.getId() != null) {
            LrRelationship fromParent = relationshipFromParent(context);
            if (fromParent instanceof LrPersistentRelationship) {
                LrPersistentRelationship r = (LrPersistentRelationship) fromParent;
                if (r.isToDependentEntity()) {
                    for (EntityUpdate<T> u : context.getUpdates()) {
                        u.getOrCreateId().putAll(r.extractId(parent.getId()));
                    }
                }
            }
        }
    }

    private LrRelationship relationshipFromParent(UpdateContext<?> context) {

        EntityParent<?> parent = context.getParent();

        if (parent == null) {
            return null;
        }

        LrRelationship r = metadataService.getLrRelationship(parent);
        if (r instanceof LrPersistentRelationship) {
            return r;
        }

        return null;
    }
}
