package io.agrest.runtime.constraints;

import io.agrest.ResourceEntity;
import io.agrest.SizeConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SizeFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SizeFilter.class);

    public static <T> void apply(ResourceEntity<T> entity, SizeConstraints constraints) {
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
