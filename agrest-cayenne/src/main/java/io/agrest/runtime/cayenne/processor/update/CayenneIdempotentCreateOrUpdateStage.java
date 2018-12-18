package io.agrest.runtime.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneIdempotentCreateOrUpdateStage extends CayenneCreateOrUpdateStage {

    public CayenneIdempotentCreateOrUpdateStage(@Inject IMetadataService metadataService) {
        super(metadataService);
    }

    @Override
    protected <T extends DataObject> void createOrUpdate(
            UpdateContext<T, Expression> context,
            ObjectRelator relator,
            Map.Entry<Object, Collection<EntityUpdate<T>>> updates) {

        // null key - each update is individual object to create;
        // explicit key - each update applies to the same object;

        if (updates.getKey() == null) {
            throw new AgException(Response.Status.BAD_REQUEST, "Request is not idempotent.");
        }

        super.createOrUpdate(context, relator, updates);
    }
}
