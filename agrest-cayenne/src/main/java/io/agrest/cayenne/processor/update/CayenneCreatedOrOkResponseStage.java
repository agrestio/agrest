package io.agrest.cayenne.processor.update;

import io.agrest.HttpStatus;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateOperationType;
import org.apache.cayenne.DataObject;

/**
 * @since 2.7
 */
public class CayenneCreatedOrOkResponseStage extends CayenneFillResponseStage {

    @Override
    protected <T extends DataObject> int getHttpStatus(UpdateContext<T> context) {

        // multi-object update can be a mix of creates and updates... Don't attempt to analyze it any further
        if (context.getUpdateOperations().size() != 1) {
            return HttpStatus.OK;
        }

        return context.getUpdateOperations().iterator().next().getType() == UpdateOperationType.CREATE
                ? HttpStatus.CREATED
                : HttpStatus.OK;
    }
}
