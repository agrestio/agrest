package io.agrest.runtime.cayenne.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.backend.util.converter.ExpressionConverter;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneCreateOrUpdateStage extends CayenneUpdateStage {

    public CayenneCreateOrUpdateStage(@Inject IMetadataService metadataService, @Inject ExpressionConverter expressionConverter) {
        super(metadataService, expressionConverter);
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
