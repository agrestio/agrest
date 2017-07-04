package com.nhl.link.rest.runtime.cayenne.processor.update;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneCreateOrUpdateStage extends CayenneUpdateStage {

    public CayenneCreateOrUpdateStage(@Inject IMetadataService metadataService) {
        super(metadataService);
    }

    @Override
    protected <T extends DataObject> void afterUpdatesMerge(
            UpdateContext<T> context,
            Map<Object, Collection<EntityUpdate<T>>> keyMap) {

        if (keyMap.isEmpty()) {
            return;
        }

        ObjectRelator relator = createRelator(context);
        keyMap.entrySet().forEach(e -> createOrUpdate(context, relator, e));
    }

    protected <T extends DataObject> void createOrUpdate(
            UpdateContext<T> context,
            ObjectRelator relator,
            Map.Entry<Object, Collection<EntityUpdate<T>>> updates) {

        for (EntityUpdate<T> u : updates.getValue()) {
            createSingle(context, relator, u);
        }
    }
}
