package io.agrest.cayenne.processor.select;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.RootDataResolverFactory;
import org.apache.cayenne.map.ObjEntity;

/**
 * @since 3.4
 */
public class CayenneRootDataResolverBuilder {

    private final ICayennePersister persister;
    private final ICayenneQueryAssembler queryAssembler;

    public CayenneRootDataResolverBuilder(ICayennePersister persister, ICayenneQueryAssembler queryAssembler) {
        this.persister = persister;
        this.queryAssembler = queryAssembler;
    }

    public RootDataResolverFactory viaQuery() {
        return this::viaQuery;
    }

    protected <T> RootDataResolver<T> viaQuery(Class<?> rootType) {
        validateRoot(rootType);
        return (RootDataResolver<T>) new ViaQueryResolver(queryAssembler, persister);
    }

    protected void validateRoot(Class<?> rootType) {
        ObjEntity entity = persister.entityResolver().getObjEntity(rootType);
        if (entity == null) {
            throw new IllegalStateException("Entity '" + rootType.getSimpleName()
                    + "' is not mapped in Cayenne and can't be resolved with a Cayenne resolver");
        }
    }
}
