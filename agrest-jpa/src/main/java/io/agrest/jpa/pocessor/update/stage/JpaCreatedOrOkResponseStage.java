package io.agrest.jpa.pocessor.update.stage;

import java.util.List;
import java.util.Map;

import io.agrest.HttpStatus;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaCreatedOrOkResponseStage extends JpaFillResponseStage {

    public JpaCreatedOrOkResponseStage(@Inject IAgJpaPersister persister) {
        super(persister);
    }

    @Override
    protected int getHttpStatus(UpdateContext<Object> context) {

        Map<ChangeOperationType, List<ChangeOperation<Object>>> ops = context.getChangeOperations();

        // if there are operations other than CREATE, just return 200
        return !ops.get(ChangeOperationType.CREATE).isEmpty()
                && ops.get(ChangeOperationType.UPDATE).isEmpty()
                && ops.get(ChangeOperationType.DELETE).isEmpty()

                ? HttpStatus.CREATED
                : HttpStatus.OK;
    }
}
