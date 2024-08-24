package io.agrest.cayenne.processor.select;

import io.agrest.RelatedResourceEntity;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.query.ColumnSelect;

import java.util.Collections;

/**
 * A related resolver that waits for the parent query to complete, and resolves its entity objects based on the collection
 * of IDs from the parent result.
 *
 * @since 3.4
 */
public class ViaQueryWithParentIdsResolver<T extends Persistent> extends ViaQueryWithParentExpResolver<T> {

    public ViaQueryWithParentIdsResolver(ICayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        super(queryAssembler, persister);
    }

    @Override
    public void onParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context) {
        // no query here... we need to have access to parent objects before we can build our query
    }

    @Override
    protected Iterable<T> doOnParentDataResolved(
            RelatedResourceEntity<T> entity,
            Iterable<?> parentData,
            SelectContext<?> context) {

        // no parents, no need to fetch children
        if (!parentData.iterator().hasNext()) {
            return Collections.emptyList();
        }

        // assemble query here, where we have access to all parent ids
        ColumnSelect<Object[]> select = queryAssembler.createQueryWithParentIdsQualifier(entity, parentData);
        if (select == null) {
            // no parents - nothing to fetch for this entity, and no need to descend into children
            return Collections.emptyList();
        }

        CayenneProcessor.getRelatedEntity(entity).setSelect(select);
        afterQueryAssembled(entity, context);
        return super.doOnParentDataResolved(entity, parentData, context);
    }
}
