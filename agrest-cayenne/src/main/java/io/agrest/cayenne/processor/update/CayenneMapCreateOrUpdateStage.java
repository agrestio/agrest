package io.agrest.cayenne.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since 4.8
 */
public class CayenneMapCreateOrUpdateStage extends CayenneMapUpdateStage {

    @Override
    protected <T extends DataObject> void collectCreateOps(
            UpdateContext<T> context,
            Map<Object, Collection<EntityUpdate<T>>> updatesByKey) {

        Collection<EntityUpdate<T>> noKeyCreate = updatesByKey.remove(null);

        // if "null" key - multiple create ops for key - one for each EntityUpdate
        // if explicit key - a single create per key with multiple EntityUpdates
        int size = noKeyCreate != null ? noKeyCreate.size() + updatesByKey.size() : updatesByKey.size();

        List<ChangeOperation<T>> createOps = new ArrayList<>(size);

        if (noKeyCreate != null) {
            for (EntityUpdate<T> u : noKeyCreate) {
                createOps.add(new ChangeOperation<>(ChangeOperationType.CREATE, null, Collections.singletonList(u)));
            }
        }

        for (Map.Entry<Object, Collection<EntityUpdate<T>>> e : updatesByKey.entrySet()) {
            createOps.add(new ChangeOperation<>(ChangeOperationType.CREATE, null, e.getValue()));
        }

        context.setChangeOperations(ChangeOperationType.CREATE, createOps);
    }
}
