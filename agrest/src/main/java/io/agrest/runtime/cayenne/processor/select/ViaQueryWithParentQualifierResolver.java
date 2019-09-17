package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.NestedResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.meta.AgAttribute;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @since 3.4
 */
public class ViaQueryWithParentQualifierResolver extends CayenneDataResolver implements NestedDataResolver<DataObject> {

    public ViaQueryWithParentQualifierResolver(
            @Inject CayenneQueryAssembler queryAssembler,
            @Inject ICayennePersister persister) {
        super(queryAssembler, persister);
    }

    @Override
    public void onParentQueryAssembled(NestedResourceEntity<DataObject> entity, SelectContext<?> context) {
        entity.setSelect(queryAssembler.createQueryWithParentQualifier(entity));
        afterQueryAssembled(entity, context);
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

        List<DataObject> result = fetch(entity);
        indexResultByParentId(entity, result);
        afterDataFetched(entity, result, context);
    }

    protected void indexResultByParentId(NestedResourceEntity<DataObject> entity, List<DataObject> result) {

        BiConsumer<AgObjectId, DataObject> resultAccum = entity.getIncoming().isToMany()
                ? (i, o) -> entity.addToManyResult(i, o)
                : (i, o) -> entity.setToOneResult(i, o);

        AgAttribute[] idAttributes = entity.getParent().getAgEntity().getIds().toArray(new AgAttribute[0]);

        for (Object o : result) {

            // TODO: use SelectQuery<Object[]>

            // position 0 - the object itself
            // position 1..N-1 - parent id components
            Object[] row = (Object[]) o;
            DataObject object = (DataObject) row[0];

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
