package io.agrest.cayenne.processor.update.stage;

import io.agrest.HttpStatus;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;

import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneCreatedOrOkResponseStage extends CayenneFillResponseStage {

    @Override
    protected <T extends DataObject> int getHttpStatus(UpdateContext<T> context) {

        Map<ChangeOperationType, List<ChangeOperation<T>>> ops = context.getChangeOperations();

        // if there are operations other than CREATE, just return 200
        return !ops.get(ChangeOperationType.CREATE).isEmpty()
                && ops.get(ChangeOperationType.UPDATE).isEmpty()
                && ops.get(ChangeOperationType.DELETE).isEmpty()

                ? HttpStatus.CREATED
                : HttpStatus.OK;
    }
}
