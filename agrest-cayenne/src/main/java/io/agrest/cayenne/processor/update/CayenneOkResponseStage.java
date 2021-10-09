package io.agrest.cayenne.processor.update;

import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;

import javax.ws.rs.core.Response;

/**
 * @since 2.7
 */
public class CayenneOkResponseStage extends CayenneFillResponseStage {

    @Override
    protected <T extends DataObject> Response.Status getStatus(UpdateContext<T> context) {
        return Response.Status.OK;
    }
}
