package io.agrest.cayenne.processor.select;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.NestedResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.property.NestedEntityListResultReader;
import io.agrest.property.NestedEntityResultReader;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.BaseNestedDataResolver;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.query.SelectQuery;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A nested resolver that builds a database query using a qualifier from the parent entity. This is the default nested
 * resolver used by Cayenne backend.
 *
 * @since 3.4
 */
public class ViaQueryWithParentExpResolver<T extends DataObject> extends BaseNestedDataResolver<T> {

    protected ICayenneQueryAssembler queryAssembler;
    protected ICayennePersister persister;

    public ViaQueryWithParentExpResolver(ICayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        this.queryAssembler = queryAssembler;
        this.persister = persister;
    }

    @Override
    protected void doOnParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        CayenneProcessor.setQuery(entity, queryAssembler.createQueryWithParentQualifier(entity));
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

        SelectQuery<Object[]> select = CayenneProcessor.getQuery(entity);
        List<Object[]> result = persister.sharedContext().select(select);
        indexResultByParentId(entity, result);

        return result.isEmpty()
                ? Collections.emptyList()
                // transform Iterable<Object[]> to Iterable<T>
                : () -> new SingleColumnIterator<>(result.iterator(), 0);
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity) {

        AgEntity<?> parentEntity = entity.getParent().getAgEntity();

        // Per #473 there may be no ID in AgEntity, but there's one in Cayenne DbEntity, so let's the one from
        // Cayenne if possible. This would allow nested prefetches even if ID is excluded from the public data.

        // Note that this ID reader should only be used inside "agrest-cayenne" and should not leak to the generic
        // part of the Agrest stack.

        PropertyReader parentIdReader = persister.entityResolver().getObjEntity(parentEntity.getName()) != null
                ? p -> ((Persistent) p).getObjectId().getIdSnapshot()
                : parentEntity.getIdReader();

        return entity.getIncoming().isToMany()
                ? new NestedEntityListResultReader(entity, parentIdReader)
                : new NestedEntityResultReader(entity, parentIdReader);
    }

    protected void indexResultByParentId(NestedResourceEntity<T> entity, List<Object[]> result) {

        BiConsumer<AgObjectId, T> resultAccum = entity.getIncoming().isToMany()
                ? (i, o) -> entity.addToManyResult(i, o)
                : (i, o) -> entity.setToOneResult(i, o);

        AgIdPart[] idAttributes = entity.getParent().getAgEntity().getIdParts().toArray(new AgIdPart[0]);

        for (Object[] row : result) {

            // position 0 - the object itself
            // position 1..N-1 - parent id components
            T object = (T) row[0];

            if (row.length == 2) {
                resultAccum.accept(new SimpleObjectId(row[1]), object);
            } else {

                Map<String, Object> idParts = new LinkedHashMap<>();
                for (int i = 1; i < row.length; i++) {
                    idParts.put(idAttributes[i - 1].getName(), row[i]);
                }

                resultAccum.accept(new CompoundObjectId(idParts), object);
            }
        }
    }
}
