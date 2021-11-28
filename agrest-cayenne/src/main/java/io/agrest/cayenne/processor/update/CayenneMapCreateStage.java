package io.agrest.cayenne.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateOperation;
import io.agrest.runtime.processor.update.UpdateOperationType;
import org.apache.cayenne.DataObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 4.8
 */
public class CayenneMapCreateStage extends CayenneMapChangesStage {

    @Override
    protected <T extends DataObject> List<UpdateOperation<T>> map(UpdateContext<T> context) {

        List<UpdateOperation<T>> ops = new ArrayList<>();
        for (EntityUpdate<T> u : context.getUpdates()) {

            // TODO: when EntityUpdate contains id, there may be multiple updates for the same key
            //    that need to be merged in a single operation to avoid commit errors... I suppose for
            //    now the users must use "createOrUpdate" if that's  anticipated instead of "create"
            ops.add(new UpdateOperation<>(UpdateOperationType.CREATE, null, Collections.singletonList(u)));
        }

        return ops;
    }
}
