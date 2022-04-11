package io.agrest.jpa.pocessor.update.stage;

import java.util.ArrayList;
import java.util.List;

import io.agrest.EntityUpdate;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * @since 5.0
 */
public class JpaMapCreateStage extends JpaMapChangesStage {

    @Override
    protected void map(UpdateContext<Object> context) {

        List<ChangeOperation<Object>> ops = new ArrayList<>(context.getUpdates().size());
        for (EntityUpdate<Object> u : context.getUpdates()) {

            // TODO: when EntityUpdate contains id, there may be multiple updates for the same key
            //    that need to be merged in a single operation to avoid commit errors... I suppose for
            //    now the users must use "createOrUpdate" if that's  anticipated instead of "create"
            ops.add(new ChangeOperation<>(ChangeOperationType.CREATE, u.getEntity(), null, u));
        }

        context.setChangeOperations(ChangeOperationType.CREATE, ops);
    }
}
