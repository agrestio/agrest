package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;

/**
 * @since 4.8
 */
public class CayenneMapIdempotentCreateOrUpdateStage extends CayenneMapCreateOrUpdateStage {

    @Override
    protected <T extends DataObject> void collectCreateOps(
            UpdateContext<T> context,
            UpdateMap<T> updateMap) {

        if (!updateMap.getNoId().isEmpty()) {
            throw AgException.badRequest("Request is not idempotent.");
        }

        super.collectCreateOps(context, updateMap);
    }
}
