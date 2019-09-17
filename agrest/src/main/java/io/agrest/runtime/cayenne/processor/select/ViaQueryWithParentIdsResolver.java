package io.agrest.runtime.cayenne.processor.select;

import io.agrest.NestedResourceEntity;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.query.SelectQuery;

import java.util.Iterator;

/**
 * @since 3.4
 */
public class ViaQueryWithParentIdsResolver extends ViaQueryWithParentQualifierResolver {

    public ViaQueryWithParentIdsResolver(CayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        super(queryAssembler, persister);
    }

    @Override
    public void onParentQueryAssembled(NestedResourceEntity<DataObject> entity, SelectContext<?> context) {
        // do nothing... we need have access to parent objects before we can build our query
    }

    @Override
    public void onParentDataResolved(
            NestedResourceEntity<DataObject> entity,
            Iterable<?> parentData,
            SelectContext<?> context) {

        // no parents, no need to fetch children
        Iterator<?> parentIt = parentData.iterator();
        if (!parentIt.hasNext()) {
            return;
        }

        // assemble query here, where we have access to all parent ids
        SelectQuery<DataObject> select = queryAssembler.createQueryWithParentIdsQualifier(entity, parentIt);
        if (select == null) {
            // no parents - nothing to fetch for this entity, and no need to descend into children
            return;
        }

        entity.setSelect(select);
        afterQueryAssembled(entity, context);
        super.onParentDataResolved(entity, parentData, context);
    }
}
