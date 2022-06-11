package io.agrest.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.cayenne.compiler.DataObjectDataReader;
import io.agrest.cayenne.processor.CayenneRelatedResourceEntityExt;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.CayenneRootResourceEntityExt;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.resolver.BaseRelatedDataResolver;
import io.agrest.resolver.ToManyFlattenedIterator;
import io.agrest.resolver.ToOneFlattenedIterator;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.query.ColumnSelect;
import org.apache.cayenne.query.ObjectSelect;

/**
 * A resolver that doesn't run its own queries, but instead amends parent node query with prefetch spec, so that the
 * objects can be read efficiently from the parent objects. Also allows to explicitly set the prefetch semantics.
 *
 * @since 3.4
 */
public class ViaParentPrefetchResolver extends BaseRelatedDataResolver<DataObject> {

    private final int prefetchSemantics;

    public ViaParentPrefetchResolver(int prefetchSemantics) {
        this.prefetchSemantics = prefetchSemantics;
    }

    @Override
    protected void doOnParentQueryAssembled(RelatedResourceEntity<DataObject> entity, SelectContext<?> context) {
        // add prefetch to the (grand)parent query

        ResourceEntity<?> parent = entity.getParent();
        String parentPath = entity.getIncoming().getName();
        if (parent instanceof RootResourceEntity) {
            addRootPrefetch((RootResourceEntity) parent, parentPath, prefetchSemantics);
        } else {
            addRelatedPrefetch((RelatedResourceEntity<?>) parent, parentPath, prefetchSemantics);
        }
    }

    @Override
    protected Iterable<DataObject> doOnParentDataResolved(
            RelatedResourceEntity<DataObject> entity,
            Iterable<?> parentData,
            SelectContext<?> context) {
        return iterableData(entity, (Iterable<DataObject>) parentData, context);
    }

    protected void addRelatedPrefetch(RelatedResourceEntity<?> entity, String path, int prefetchSemantics) {
        CayenneRelatedResourceEntityExt ext = CayenneProcessor.getRelatedEntity(entity);
        if (ext == null) {
            throw AgException.internalServerError(
                    "Entity '%s' is not handled by Cayenne. Can not use prefetch resolver for path '%s'",
                    entity.getAgEntity().getName(),
                    path);
        }

        ColumnSelect<Object[]> select = ext.getSelect();
        if (select != null) {
            select.prefetch(path, prefetchSemantics);
            return;
        }

        ResourceEntity<?> parent = entity.getParent();
        String parentPath = entity.getIncoming().getName() + "." + path;
        if (parent instanceof RootResourceEntity) {
            addRootPrefetch((RootResourceEntity) parent, parentPath, prefetchSemantics);
        } else {
            addRelatedPrefetch((RelatedResourceEntity<?>) parent, parentPath, prefetchSemantics);
        }
    }

    protected void addRootPrefetch(RootResourceEntity<?> entity, String path, int prefetchSemantics) {

        CayenneRootResourceEntityExt<?> ext = CayenneProcessor.getRootEntity(entity);
        if (ext == null) {
            throw AgException.internalServerError(
                    "Entity '%s' is not handled by Cayenne. Can not use prefetch resolver for path '%s'",
                    entity.getAgEntity().getName(),
                    path);
        }

        ObjectSelect<?> select = ext.getSelect();
        if (select != null) {
            select.prefetch(path, prefetchSemantics);
            return;
        }

        throw AgException.internalServerError(
                "Can't add prefetch to root entity '%s' that has no query of its own. Path: %s",
                entity.getAgEntity().getName(),
                path);
    }


    @Override
    public DataReader dataReader(RelatedResourceEntity<DataObject> entity, ProcessingContext<?> context) {

        // TODO: what about multi-step prefetches? How do we locate parent then?

        // assuming the parent is a DataObject. CayenneRelatedDataResolverBuilder ensures that it is
        return DataObjectDataReader.reader(entity.getIncoming().getName());
    }

    protected Iterable<DataObject> iterableData(
            RelatedResourceEntity<DataObject> entity,
            Iterable<? extends DataObject> parentData,
            ProcessingContext<?> context) {
        DataReader reader = this.dataReader(entity, context);
        return entity.getIncoming().isToMany()
                ? () -> new ToManyFlattenedIterator<>(parentData.iterator(), reader)
                : () -> new ToOneFlattenedIterator<>(parentData.iterator(), reader);
    }
}
