package com.nhl.link.rest.runtime.cayenne.processor2.select;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.processor2.ProcessorOutcome;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @since 2.7
 */
public class CayenneFetchDataStage implements Processor<SelectContext<?>> {

    private ObjectContext sharedContext;

    public CayenneFetchDataStage(@Inject ICayennePersister persister) {
        this.sharedContext = persister.sharedContext();
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        SelectQuery<T> select = context.getSelect();

        List<T> objects = sharedContext.select(select);

        if (context.isAtMostOneObject() && objects.size() != 1) {

            LrEntity<?> entity = context.getEntity().getLrEntity();

            if (objects.isEmpty()) {
                throw new LinkRestException(Response.Status.NOT_FOUND,
                        String.format("No object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            } else {
                throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR, String.format(
                        "Found more than one object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            }
        }
        context.setObjects(objects);
    }
}
