package io.agrest.jpa.pocessor.select;

import java.util.Collections;
import java.util.Iterator;

import io.agrest.NestedResourceEntity;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.jpa.query.JpaQueryBuilder;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * A nested resolver that waits for the parent query to complete, and resolves its entity objects based on the collection
 * of IDs from the parent result.
 *
 * @since 5.0
 */
public class ViaQueryWithParentIdsResolver<T> extends ViaQueryWithParentExpResolver<T> {

    public ViaQueryWithParentIdsResolver(IJpaQueryAssembler queryAssembler, IAgJpaPersister persister) {
        super(queryAssembler, persister);
    }

    @Override
    public void onParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        // no query here... we need to have access to parent objects before we can build our query
    }

    @Override
    protected Iterable<T> doOnParentDataResolved(
            NestedResourceEntity<T> entity,
            Iterable<?> parentData,
            SelectContext<?> context) {

        // no parents, no need to fetch children
        Iterator<?> parentIt = parentData.iterator();
        if (!parentIt.hasNext()) {
            return Collections.emptyList();
        }

        // assemble query here, where we have access to all parent ids
        JpaQueryBuilder select = queryAssembler.createQueryWithParentIdsQualifier(entity, parentIt);
        if (select == null) {
            // no parents - nothing to fetch for this entity, and no need to descend into children
            return Collections.emptyList();
        }

        // FIXME
        JpaProcessor.getNestedEntity(entity).setSelect(select);
        afterQueryAssembled(entity, context);
        return super.doOnParentDataResolved(entity, parentData, context);
    }
}
