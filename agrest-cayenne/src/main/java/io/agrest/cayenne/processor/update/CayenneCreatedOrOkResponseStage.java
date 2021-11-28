package io.agrest.cayenne.processor.update;

import io.agrest.HttpStatus;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import org.apache.cayenne.DataObject;

import java.util.List;

/**
 * @since 2.7
 */
public class CayenneCreatedOrOkResponseStage extends CayenneFillResponseStage {

    @Override
    protected <T extends DataObject> int getHttpStatus(UpdateContext<T> context) {

        // if there are operations other than CREATE, just return 200
        if (context.getUpdateOperations().size() != 1) {
            return HttpStatus.OK;
        }

        // see if the only operation available is CREATE
        List<ChangeOperation<T>> created = context.getUpdateOperations().get(ChangeOperationType.CREATE);
        return created != null && !created.isEmpty()
                ? HttpStatus.CREATED
                : HttpStatus.OK;
    }
}
