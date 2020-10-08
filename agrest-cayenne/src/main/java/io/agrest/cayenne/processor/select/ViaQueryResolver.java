package io.agrest.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.meta.AgEntity;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * A root resolver that builds and executes a Cayenne DB query based on the request parameters provided by the client.
 *
 * @since 3.4
 */
public class ViaQueryResolver<T extends DataObject> extends BaseRootDataResolver<T> {

    protected CayenneQueryAssembler queryAssembler;
    protected ICayennePersister persister;

    public ViaQueryResolver(CayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        this.queryAssembler = queryAssembler;
        this.persister = persister;
    }

    @Override
    protected void doAssembleQuery(SelectContext<T> context) {
        CayenneProcessor.setQuery(context.getEntity(), queryAssembler.createRootQuery(context));
    }

    @Override
    protected List<T> doFetchData(SelectContext<T> context) {
        SelectQuery<T> select = CayenneProcessor.getQuery(context.getEntity());
        List<T> result = persister.sharedContext().select(select);
        checkObjectNotFound(context, result);
        return result;
    }

    protected void checkObjectNotFound(SelectContext<T> context, List<?> result) {
        if (context.isAtMostOneObject() && result.size() != 1) {

            AgEntity<?> entity = context.getEntity().getAgEntity();

            if (result.isEmpty()) {
                throw new AgException(Response.Status.NOT_FOUND,
                        String.format("No object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            } else {
                throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, String.format(
                        "Found more than one object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            }
        }
    }
}
