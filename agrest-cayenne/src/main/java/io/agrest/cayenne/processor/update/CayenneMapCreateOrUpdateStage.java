package io.agrest.cayenne.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import org.apache.cayenne.DataObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since 4.8
 */
public class CayenneMapCreateOrUpdateStage extends CayenneMapUpdateStage {

    @Override
    protected <T extends DataObject> void processUnmapped(
            UpdateContext<T> context,
            Map<Object, Collection<EntityUpdate<T>>> updatesByKey,
            List<ChangeOperation<T>> ops) {

        for (Map.Entry<Object, Collection<EntityUpdate<T>>> e : updatesByKey.entrySet()) {

            // if "null" key - separate create ops for each EntityUpdate
            // if explicit key - a single create for multiple EntityUpdates

            if (e.getKey() == null) {
                for (EntityUpdate<T> u : e.getValue()) {
                    ops.add(new ChangeOperation<>(ChangeOperationType.CREATE, null, Collections.singletonList(u)));
                }
            } else {
                ops.add(new ChangeOperation<>(ChangeOperationType.CREATE, null, e.getValue()));
            }
        }
    }
}
