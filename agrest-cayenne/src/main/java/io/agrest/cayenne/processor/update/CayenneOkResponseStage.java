package io.agrest.cayenne.processor.update;

import io.agrest.HttpStatus;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;

/**
 * @since 2.7
 */
public class CayenneOkResponseStage extends CayenneFillResponseStage {

    @Override
    protected <T extends DataObject> int getHttpStatus(UpdateContext<T> context) {
        return HttpStatus.OK;
    }
}
