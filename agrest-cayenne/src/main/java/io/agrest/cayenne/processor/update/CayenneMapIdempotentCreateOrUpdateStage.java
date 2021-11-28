package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;

import java.util.Collection;
import java.util.Map;

/**
 * @since 4.8
 */
public class CayenneMapIdempotentCreateOrUpdateStage extends CayenneMapCreateOrUpdateStage {

    @Override
    protected <T extends DataObject> void collectCreateOps(
            UpdateContext<T> context,
            Map<Object, Collection<EntityUpdate<T>>> updatesByKey) {

        if (updatesByKey.containsKey(null)) {
            throw AgException.badRequest("Request is not idempotent.");
        }

        super.collectCreateOps(context, updatesByKey);
    }
}
