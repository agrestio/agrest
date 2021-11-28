package io.agrest.cayenne.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since 4.8
 */
public class CayenneMapCreateOrUpdateStage extends CayenneMapUpdateStage {

    @Override
    protected <T extends DataObject> void collectCreateOps(
            UpdateContext<T> context,
            UpdateMap<T> updateMap) {

        List<EntityUpdate<T>> noKeyCreate = updateMap.getNoId();
        Collection<EntityUpdate<T>> withKeyCreate = updateMap.getWithId();

        List<ChangeOperation<T>> createOps = new ArrayList<>(noKeyCreate.size() + withKeyCreate.size());

        for (EntityUpdate<T> u : noKeyCreate) {
            createOps.add(new ChangeOperation<>(ChangeOperationType.CREATE, null, u));
        }

        for (EntityUpdate<T> u : withKeyCreate) {
            createOps.add(new ChangeOperation<>(ChangeOperationType.CREATE, null, u));
        }

        context.setChangeOperations(ChangeOperationType.CREATE, createOps);
    }
}
