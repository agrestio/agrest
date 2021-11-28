package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateOperation;
import org.apache.cayenne.DataObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 4.8
 */
public class CayenneMapIdempotentCreateOrUpdateStage extends CayenneMapCreateOrUpdateStage {

    @Override
    protected <T extends DataObject> void processUnmapped(
            UpdateContext<T> context,
            Map<Object, Collection<EntityUpdate<T>>> updatesByKey,
            List<UpdateOperation<T>> ops) {

        if (updatesByKey.containsKey(null)) {
            throw AgException.badRequest("Request is not idempotent.");
        }

        super.processUnmapped(context, updatesByKey, ops);
    }
}
