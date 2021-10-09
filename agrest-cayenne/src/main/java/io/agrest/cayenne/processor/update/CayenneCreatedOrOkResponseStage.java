package io.agrest.cayenne.processor.update;

import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;

import javax.ws.rs.core.Response;

/**
 * @since 2.7
 */
public class CayenneCreatedOrOkResponseStage extends CayenneFillResponseStage {

    @Override
    protected <T extends DataObject> Response.Status getStatus(UpdateContext<T> context) {

        // multi-object update can be a mix of creates and updates... Don't attempt to analyze it any further
        if (context.getUpdates().size() != 1) {
            return Response.Status.OK;
        }

        return context.getFirst().isCreatedNew() ? Response.Status.CREATED : Response.Status.OK;
    }
}
