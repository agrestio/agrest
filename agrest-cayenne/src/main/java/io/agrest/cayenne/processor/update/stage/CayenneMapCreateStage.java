package io.agrest.cayenne.processor.update.stage;

import io.agrest.EntityUpdate;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.Persistent;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 4.8
 */
public class CayenneMapCreateStage extends CayenneMapChangesStage {

    @Override
    protected <T extends Persistent> void map(UpdateContext<T> context) {

        List<ChangeOperation<T>> ops = new ArrayList<>(context.getUpdates().size());
        for (EntityUpdate<T> u : context.getUpdates()) {

            // TODO: when EntityUpdate contains id, there may be multiple updates for the same key
            //    that need to be merged in a single operation to avoid commit errors... I suppose for
            //    now the users must use "createOrUpdate" if that's  anticipated instead of "create"
            ops.add(new ChangeOperation<>(ChangeOperationType.CREATE, u.getEntity(), null, u));
        }

        context.setChangeOperations(ChangeOperationType.CREATE, ops);
    }
}
