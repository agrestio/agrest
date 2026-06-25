package io.agrest.cayenne.processor.update.stage;

import io.agrest.HttpStatus;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.Persistent;

/**
 * @since 2.7
 */
public class CayenneCreatedResponseStage extends CayenneFillResponseStage {

    @Override
    protected <T extends Persistent> int getHttpStatus(UpdateContext<T> context) {
        return HttpStatus.CREATED;
    }
}
