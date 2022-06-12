package io.agrest.cayenne.processor.select;

import io.agrest.id.AgObjectId;
import io.agrest.RelatedResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.ToOneResourceEntity;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.CayenneRelatedResourceEntityExt;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.reader.ToManyEntityResultReader;
import io.agrest.reader.ToOneEntityResultReader;
import io.agrest.resolver.BaseRelatedDataResolver;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A related resolver that builds a database query using a qualifier from the parent entity. This is the default related
 * resolver used by Cayenne backend.
 *
 * @since 3.4
 */
public class ViaQueryWithParentExpResolver<T extends DataObject> extends BaseRelatedDataResolver<T> {

    protected ICayenneQueryAssembler queryAssembler;
    protected ICayennePersister persister;

    public ViaQueryWithParentExpResolver(ICayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        this.queryAssembler = queryAssembler;
        this.persister = persister;
    }

    @Override
    protected void doOnParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context) {
        CayenneProcessor.getRelatedEntity(entity)
                .setSelect(queryAssembler.createQueryWithParentQualifier(entity));
    }

    @Override
    protected Iterable<T> doOnParentDataResolved(
            RelatedResourceEntity<T> entity,
            Iterable<?> parentData,
            SelectContext<?> context) {

        // no parents, no need to fetch children
        Iterator<?> parentIt = parentData.iterator();
        if (!parentIt.hasNext()) {
            return Collections.emptyList();
        }

        // TODO: the actual query is a column query instead of T, hence ugly generics stripping
        CayenneRelatedResourceEntityExt ext = CayenneProcessor.getRelatedEntity(entity);

        List<Object[]> result = ext.getSelect().select(persister.sharedContext());
        indexResultByParentId(entity, result);

        return result.isEmpty()
                ? Collections.emptyList()
                // transform Iterable<Object[]> to Iterable<T>
                : () -> new SingleColumnIterator<>(result.iterator(), 0);
    }

    @Override
    public DataReader dataReader(RelatedResourceEntity<T> entity, ProcessingContext<?> context) {

        AgEntity<?> parentEntity = entity.getParent().getAgEntity();

        DataReader parentIdReader = parentEntity.getIdReader();

        return entity instanceof ToManyResourceEntity
                ? new ToManyEntityResultReader((ToManyResourceEntity) entity, parentIdReader)
                : new ToOneEntityResultReader((ToOneResourceEntity<?>) entity, parentIdReader);
    }

    protected void indexResultByParentId(RelatedResourceEntity<T> entity, List<Object[]> result) {

        AgIdPart[] idAttributes = entity.getParent().getAgEntity().getIdParts().toArray(new AgIdPart[0]);

        for (Object[] row : result) {

            // position 0 - the object itself
            // position 1..N-1 - parent id components
            T object = (T) row[0];

            if (row.length == 2) {
                entity.addData(AgObjectId.of(row[1]), object);
            } else {

                Map<String, Object> idParts = new LinkedHashMap<>();
                for (int i = 1; i < row.length; i++) {
                    idParts.put(idAttributes[i - 1].getName(), row[i]);
                }

                entity.addData(AgObjectId.ofMap(idParts), object);
            }
        }
    }
}
