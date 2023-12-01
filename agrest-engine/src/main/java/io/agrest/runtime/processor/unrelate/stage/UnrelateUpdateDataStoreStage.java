package io.agrest.runtime.processor.unrelate.stage;

import io.agrest.AgException;
import io.agrest.UnrelateStage;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IIdResolver;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class UnrelateUpdateDataStoreStage implements Processor<UnrelateContext<?>> {

    private final IIdResolver idResolver;

    public UnrelateUpdateDataStoreStage(@Inject IIdResolver idResolver) {
        this.idResolver = idResolver;
    }

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        resolveIds(context);
        unrelate(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected void resolveIds(UnrelateContext<?> context) {
        AgEntity<?> srcEntity = context.getSchema().getEntity(context.getType());
        AgRelationship relationship = srcEntity.getRelationship(context.getRelationship());
        if (relationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", context.getRelationship());
        }

        AgEntity<?> targetEntity = relationship.getTargetEntity();

        context.setSourceId(idResolver.resolve(srcEntity, context.getUnresolvedSourceId()));
        context.setTargetId(idResolver.resolve(targetEntity, context.getUnresolvedTargetId()));
    }

    protected void unrelate(UnrelateContext<?> context) {
        throw new UnsupportedOperationException(
                "No implementation of " + UnrelateStage.UPDATE_DATA_STORE + " stage is available");
    }
}
