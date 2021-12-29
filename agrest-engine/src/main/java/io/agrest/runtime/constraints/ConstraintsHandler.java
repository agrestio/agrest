package io.agrest.runtime.constraints;

import io.agrest.ResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.runtime.processor.update.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link IConstraintsHandler} that ensures that no target attributes exceed
 * the defined bounds.
 *
 * @since 1.5
 */
public class ConstraintsHandler implements IConstraintsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintsHandler.class);

    private EntityConstraintHandler modelConstraintHandler;

    public ConstraintsHandler() {
        this.modelConstraintHandler = new EntityConstraintHandler();
    }

    @Override
    public <T> void constrainUpdate(UpdateContext<T> context) {
        modelConstraintHandler.constrainUpdate(context);
    }

    @Override
    public <T> void constrainResponse(ResourceEntity<T> entity, SizeConstraints sizeConstraints) {

        if (sizeConstraints != null) {
            applySizeConstraintsForRead(entity, sizeConstraints);
        }

        modelConstraintHandler.constrainResponse(entity);
    }

    protected void applySizeConstraintsForRead(ResourceEntity<?> entity, SizeConstraints constraints) {

        // fetchOffset - do not exceed source offset
        int upperOffset = constraints.getFetchOffset();
        if (upperOffset > 0 && (entity.getFetchOffset() < 0 || entity.getFetchOffset() > upperOffset)) {
            LOGGER.info("Reducing fetch offset from " + entity.getFetchOffset() + " to max allowed value of "
                    + upperOffset);
            entity.setFetchOffset(upperOffset);
        }

        // fetchLimit - do not exceed source limit
        int upperLimit = constraints.getFetchLimit();
        if (upperLimit > 0 && (entity.getFetchLimit() <= 0 || entity.getFetchLimit() > upperLimit)) {
            LOGGER.info(
                    "Reducing fetch limit from " + entity.getFetchLimit() + " to max allowed value of " + upperLimit);
            entity.setFetchLimit(upperLimit);
        }
    }

}
