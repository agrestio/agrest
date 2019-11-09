package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.NestedResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.meta.AgAttribute;
import io.agrest.property.NestedEntityListResultReader;
import io.agrest.property.NestedEntityResultReader;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.BaseNestedDataResolver;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A nested resolver that builds a database query using a qualifier from the parent entity. This is the default nested
 * resolver used by Cayenne backend.
 *
 * @since 3.4
 */
public class ViaQueryWithParentExpResolver extends BaseNestedDataResolver<DataObject> {

    protected CayenneQueryAssembler queryAssembler;
    protected ICayennePersister persister;

    public ViaQueryWithParentExpResolver(CayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        this.queryAssembler = queryAssembler;
        this.persister = persister;
    }

    protected void validateParent(NestedResourceEntity<?> entity) {
        // sanity check ... while child type validity is ensured by the API, parent can be anything, so let's ensure
        // it is also a DataObject
        Class<?> parentType = entity.getParent().getType();
        if (persister.entityResolver().getObjEntity(parentType) == null) {
            throw new IllegalStateException("Entity '" + parentType.getSimpleName()
                    + "' is not mapped in Cayenne, so its child '"
                    + entity.getType().getSimpleName()
                    + "' can't be resolved with " + getClass().getName());
        }
    }

    @Override
    protected void doOnParentQueryAssembled(NestedResourceEntity<DataObject> entity, SelectContext<?> context) {
        validateParent(entity);
        entity.setSelect(queryAssembler.createQueryWithParentQualifier(entity));
    }

    @Override
    protected Iterable<DataObject> doOnParentDataResolved(
            NestedResourceEntity<DataObject> entity,
            Iterable<?> parentData,
            SelectContext<?> context) {

        // no parents, no need to fetch children
        Iterator<?> parentIt = parentData.iterator();
        if (!parentIt.hasNext()) {
            return Collections.emptyList();
        }

        List<DataObject> result = persister.sharedContext().select(entity.getSelect());
        indexResultByParentId(entity, result);

        return result;
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<DataObject> entity) {
        return entity.getIncoming().isToMany()
                ? new NestedEntityListResultReader(entity)
                : new NestedEntityResultReader(entity);
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
