package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.meta.AgEntity;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * A root resolver that builds and executes a Cayenne DB query based on the request parameters provided by the client.
 *
 * @since 3.4
 */
public class ViaQueryResolver extends BaseRootDataResolver<DataObject> {

    protected CayenneQueryAssembler queryAssembler;
    protected ICayennePersister persister;

    public ViaQueryResolver(CayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        this.queryAssembler = queryAssembler;
        this.persister = persister;
    }

    @Override
    protected void doAssembleQuery(SelectContext<DataObject> context) {
        context.getEntity().setSelect(queryAssembler.createRootQuery(context));
    }

    @Override
    protected List<DataObject> doFetchData(SelectContext<DataObject> context) {
        List<DataObject> result = persister.sharedContext().select(context.getEntity().getSelect());
        checkObjectNotFound(context, result);
        return result;
    }

    protected void checkObjectNotFound(SelectContext<DataObject> context, List<?> result) {
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
