package io.agrest.runtime.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.backend.util.converter.ExpressionConverter;
import io.agrest.runtime.cayenne.converter.CayenneExpressionConverter;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneIdempotentFullSyncStage extends CayenneIdempotentCreateOrUpdateStage {

    public CayenneIdempotentFullSyncStage(@Inject IMetadataService metadataService, @Inject ExpressionConverter expressionConverter) {
        super(metadataService, expressionConverter);
    }

    @Override
    protected <T extends DataObject> void sync(UpdateContext<T> context) {

        ObjectMapper<T> mapper = createObjectMapper(context);
        Map<Object, Collection<EntityUpdate<T>>> keyMap = mutableKeyMap(context, mapper);

        List<T> allObjects = allItems(context);

        List<DataObject> deletedObjects = new ArrayList<>();

        for (T o : allObjects) {
            Object key = mapper.keyForObject(o);

            Collection<EntityUpdate<T>> updates = keyMap.remove(key);

            if (updates == null) {
                deletedObjects.add(o);
            } else {
                updateSingle(context, o, updates);
            }
        }

        if (!deletedObjects.isEmpty()) {
            CayenneUpdateStartStage.cayenneContext(context).deleteObjects(deletedObjects);
        }

        // check leftovers - those correspond to objects missing in the DB or
        // objects with no keys
        afterUpdatesMerge(context, keyMap);
    }

    <T extends DataObject> List<T> allItems(UpdateContext<T> context) {
        SelectQuery<T> query = SelectQuery.query(context.getType());

        // apply various request filters identifying the span of the collection

        if (context.getParent() != null) {
            EntityResolver resolver = CayenneUpdateStartStage.cayenneContext(context).getEntityResolver();
            query.andQualifier(context.getParent().qualifier(resolver));
        }

        if (context.getEntity().getQualifier() != null) {
            query.andQualifier(expressionConverter.apply(context.getEntity().getQualifier()));
        }

        // TODO: use SelectBuilder to get Cayenne representation of the
        // resource, instead of duplicating this here...

        List<T> objects = CayenneUpdateStartStage.cayenneContext(context).select(query);
        if (context.isById() && objects.size() > 1) {
            throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, String.format(
                    "Found more than one object for ID '%s' and entity '%s'",
                    context.getId(), context.getEntity().getAgEntity().getName()));
        }
        return objects;
    }



}
