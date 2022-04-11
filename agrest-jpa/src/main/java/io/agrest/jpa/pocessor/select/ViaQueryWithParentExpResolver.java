package io.agrest.jpa.pocessor.select;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.agrest.CompoundObjectId;
import io.agrest.NestedResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.ToManyResourceEntity;
import io.agrest.ToOneResourceEntity;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.JpaNestedResourceEntityExt;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.processor.ProcessingContext;
import io.agrest.property.PropertyReader;
import io.agrest.property.ToManyEntityResultReader;
import io.agrest.property.ToOneEntityResultReader;
import io.agrest.resolver.BaseNestedDataResolver;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * A nested resolver that builds a database query using a qualifier from the parent entity. This is the default nested
 * resolver used by Cayenne backend.
 *
 * @since 5.0
 */
public class ViaQueryWithParentExpResolver<T> extends BaseNestedDataResolver<T> {

    protected IJpaQueryAssembler queryAssembler;
    protected IAgJpaPersister persister;

    public ViaQueryWithParentExpResolver(IJpaQueryAssembler queryAssembler, IAgJpaPersister persister) {
        this.queryAssembler = queryAssembler;
        this.persister = persister;
    }

    @Override
    protected void doOnParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        JpaProcessor.getNestedEntity(entity)
                .setSelect(queryAssembler.createQueryWithParentQualifier(entity));
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

        JpaNestedResourceEntityExt ext = JpaProcessor.getNestedEntity(entity);
        @SuppressWarnings("unchecked")
        List<Object[]> result = (List<Object[]>)ext.getSelect().getResultList();
        indexResultByParentId(entity, result);

        return result.isEmpty()
                ? Collections.emptyList()
                // transform Iterable<Object[]> to Iterable<T>
                : () -> new SingleColumnIterator<>(result.iterator(), 0);
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity, ProcessingContext<?> context) {

        AgEntity<?> parentEntity = entity.getParent().getAgEntity();

        PropertyReader parentIdReader = parentEntity.getIdReader();

        return entity instanceof ToManyResourceEntity
                ? new ToManyEntityResultReader((ToManyResourceEntity<?>) entity, parentIdReader)
                : new ToOneEntityResultReader((ToOneResourceEntity<?>) entity, parentIdReader);
    }

    protected void indexResultByParentId(NestedResourceEntity<T> entity, List<Object[]> result) {

        AgIdPart[] idAttributes = entity.getParent().getAgEntity().getIdParts().toArray(new AgIdPart[0]);

        for (Object[] row : result) {

            // position 0 - the object itself
            // position 1..N-1 - parent id components
            T object = (T) row[0];

            if (row.length == 2) {
                entity.addData(new SimpleObjectId(row[1]), object);
            } else {

                Map<String, Object> idParts = new LinkedHashMap<>();
                for (int i = 1; i < row.length; i++) {
                    idParts.put(idAttributes[i - 1].getName(), row[i]);
                }

                entity.addData(new CompoundObjectId(idParts), object);
            }
        }
    }
}
