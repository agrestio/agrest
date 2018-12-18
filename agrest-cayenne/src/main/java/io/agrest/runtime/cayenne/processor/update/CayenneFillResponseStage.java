package io.agrest.runtime.cayenne.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.exp.Expression;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since 2.7
 */
public class CayenneFillResponseStage implements Processor<UpdateContext<?, ?>> {

    private Response.Status status;

    public CayenneFillResponseStage(Response.Status status) {
        this.status = status;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?, ?> context) {
        doExecute((UpdateContext<DataObject, Expression>) context);
        return ProcessorOutcome.CONTINUE;
    }

    @SuppressWarnings("unchecked")
    protected <T extends DataObject> void doExecute(UpdateContext<T, Expression> context) {

        context.setStatus(status);

        // response objects are attached to EntityUpdate instances ... if
        // 'includeData' is true create a list of unique updated objects in the
        // order corresponding to their initial appearance in the update.
        // We do not have to guarantee the order of objects in response (and
        // only Sencha seems to care - see #46), but there's not much overhead
        // involved, so we are doing it for all clients, not just Sencha

        if (context.isIncludingDataInResponse()) {

            // if there are dupes, the list size will be smaller... sizing it
            // pessimistically
            List<T> objects = new ArrayList<>(context.getUpdates().size());

            // 'seen' is for a less common case of multiple updates per object
            // in a request
            Set<ObjectId> seen = new HashSet<>();

            for (EntityUpdate<T> u : context.getUpdates()) {

                T o = (T) u.getMergedTo();
                if (o != null && seen.add(o.getObjectId())) {
                    objects.add(o);
                }
            }

            context.setObjects(objects);
        }
    }
}
