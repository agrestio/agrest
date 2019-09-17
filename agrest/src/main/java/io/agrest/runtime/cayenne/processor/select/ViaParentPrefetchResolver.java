package io.agrest.runtime.cayenne.processor.select;

import io.agrest.NestedResourceEntity;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;

import java.util.Collections;

/**
 * @since 3.4
 */
public class ViaParentPrefetchResolver extends CayenneDataResolver implements NestedDataResolver<DataObject> {

    public ViaParentPrefetchResolver(CayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        super(queryAssembler, persister);
    }

    @Override
    public void onParentQueryAssembled(
            NestedResourceEntity<DataObject> entity,
            SelectContext<?> context) {

        addPrefetch(entity);
        afterQueryAssembled(entity, context);
    }

    @Override
    public void onParentDataResolved(
            NestedResourceEntity<DataObject> entity,
            Iterable<?> parentData,
            SelectContext<?> context) {

        // all the data was fetched at the parent level... so pass an empty list

        // Current limitation - if used for non-leaf node, all of its children must also use ViaParentPrefetchResolver.
        // Otherwise there will be a child data loss.
        // TODO: We need to efficiently emulate Iterable<ThisEntity> over parent data...

        afterDataFetched(entity, Collections.emptyList(), context);
    }
}
