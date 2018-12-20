package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.backend.util.converter.OrderingConverter;
import io.agrest.meta.AgEntity;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.IAgPersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @since 2.7
 */
public class CayenneFetchDataStage extends CayenneAssembleQueryStage {

    private IAgPersister<ObjectContext, EntityResolver> persister;

    public CayenneFetchDataStage(@Inject IAgPersister persister,
                                 @Inject OrderingConverter orderingConverter) {

        // Store persister, don't extract ObjectContext from it right away.
        // Such deferred initialization may help building POJO pipelines.
        super(persister, orderingConverter);
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?, ?> context) {
        doExecute((SelectContext<DataObject, Expression>)context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T extends DataObject> void doExecute(SelectContext<T, Expression> context) {
        SelectQuery<T> select = buildQuery(context);

        List<T> objects = persister.sharedContext().select(select);

        if (context.isAtMostOneObject() && objects.size() != 1) {

            AgEntity<?> entity = context.getEntity().getAgEntity();

            if (objects.isEmpty()) {
                throw new AgException(Response.Status.NOT_FOUND,
                        String.format("No object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            } else {
                throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, String.format(
                        "Found more than one object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            }
        }
        context.setObjects(objects);
    }
}
