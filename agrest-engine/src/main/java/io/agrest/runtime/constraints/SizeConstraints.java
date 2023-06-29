package io.agrest.runtime.constraints;

import io.agrest.ResourceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SizeConstraints {

    private static final Logger LOGGER = LoggerFactory.getLogger(SizeConstraints.class);

    public static <T> void apply(ResourceEntity<T> entity, io.agrest.SizeConstraints constraints) {
        if (constraints != null) {

            // fetchOffset - do not exceed source offset
            int upperOffset = constraints.getFetchOffset();
            if (upperOffset > 0 && (entity.getStart() < 0 || entity.getStart() > upperOffset)) {
                LOGGER.info("Reducing fetch offset from " + entity.getStart() + " to max allowed value of "
                        + upperOffset);
                entity.setStart(upperOffset);
            }

            // fetchLimit - do not exceed source limit
            int upperLimit = constraints.getFetchLimit();
            if (upperLimit > 0 && (entity.getLimit() <= 0 || entity.getLimit() > upperLimit)) {
                LOGGER.info(
                        "Reducing fetch limit from " + entity.getLimit() + " to max allowed value of " + upperLimit);
                entity.setLimit(upperLimit);
            }
        }
    }
}
