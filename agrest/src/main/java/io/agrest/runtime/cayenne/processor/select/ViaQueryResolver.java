package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.RootResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.resolver.RootDataResolver;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @since 3.4
 */
public class ViaQueryResolver extends CayenneDataResolver implements RootDataResolver<DataObject> {

    public ViaQueryResolver(
            @Inject CayenneQueryAssembler queryAssembler,
            @Inject ICayennePersister persister) {
        super(queryAssembler, persister);
    }

    @Override
    public void assembleQuery(SelectContext<DataObject> context) {
        context.getEntity().setSelect(queryAssembler.createRootQuery(context));
        afterQueryAssembled(context.getEntity(), context);
    }

    @Override
    public void fetchData(SelectContext<DataObject> context) {
        RootResourceEntity<DataObject> entity = context.getEntity();
        List<DataObject> result = fetch(entity);
        entity.setResult(result);
        checkObjectNotFound(context, result);
        afterDataFetched(entity, result, context);
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
