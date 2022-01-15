package io.agrest.cayenne.processor.select;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.resolver.ContextAwareRootDataResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.RootDataResolverFactory;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.map.ObjEntity;

/**
 * @since 3.4
 */
public class CayenneRootDataResolverBuilder {

    public RootDataResolverFactory viaQuery() {
        return this::viaQuery;
    }

    protected <T> RootDataResolver<T> viaQuery(Class<T> rootType) {
        return new ContextAwareRootDataResolver<>(c -> viaQuery(rootType, c));
    }

    protected <T> RootDataResolver<T> viaQuery(Class<T> rootType, SelectContext<T> context) {

        ICayennePersister persister = context.service(ICayennePersister.class);
        ICayenneQueryAssembler queryAssembler = context.service(ICayenneQueryAssembler.class);
        validateRoot(rootType, persister);

        return (RootDataResolver<T>) new ViaQueryResolver(queryAssembler, persister);
    }

    protected void validateRoot(Class<?> rootType, ICayennePersister persister) {
        ObjEntity entity = persister.entityResolver().getObjEntity(rootType);
        if (entity == null) {
            throw new IllegalStateException("Entity '" + rootType.getSimpleName()
                    + "' is not mapped in Cayenne and can't be resolved with a Cayenne resolver");
        }
    }
}
