package com.nhl.link.rest.runtime.cayenne.processor.update;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

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
            UpdateContext<T> context,
            ObjectRelator relator,
            Map.Entry<Object, Collection<EntityUpdate<T>>> updates) {

        // null key - each update is individual object to create;
        // explicit key - each update applies to the same object;

        if (updates.getKey() == null) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Request is not idempotent.");
        }

        super.createOrUpdate(context, relator, updates);
    }
}
