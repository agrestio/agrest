package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.meta.AgDataMap;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneIdempotentCreateOrUpdateStage extends CayenneCreateOrUpdateStage {

    public CayenneIdempotentCreateOrUpdateStage(
            @Inject AgDataMap dataMap,
            @Inject ICayennePersister persister) {
        super(dataMap, persister);
    }

    @Override
    protected <T extends DataObject> void afterUpdatesMerge(
            UpdateContext<T> context,
            ObjectRelator relator,
            Map<Object, Collection<EntityUpdate<T>>> keyMap) {

        if (keyMap.containsKey(null)) {
            throw AgException.badRequest("Request is not idempotent.");
        }

        super.afterUpdatesMerge(context, relator, keyMap);
    }
}
