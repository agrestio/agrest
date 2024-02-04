package io.agrest.cayenne.compiler;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.processor.select.ContextualCayenneRelatedDataResolver;
import io.agrest.cayenne.processor.select.ViaQueryResolver;
import io.agrest.cayenne.processor.select.ViaQueryWithParentExpResolver;
import io.agrest.cayenne.processor.select.ViaQueryWithParentIdsResolver;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.meta.AgSchema;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.LazyEntity;
import io.agrest.resolver.RelatedDataResolver;
import io.agrest.resolver.RootDataResolver;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @since 4.1
 */
public class CayenneAgEntityCompiler implements AgEntityCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CayenneAgEntityCompiler.class);

    private final EntityResolver cayenneEntityResolver;
    private final Map<String, AgEntityOverlay<?>> entityOverlays;
    private final RootDataResolver<?> defaultRootResolver;
    private final RelatedDataResolver<?> defaultRelatedDataResolver;

    public CayenneAgEntityCompiler(
            @Inject ICayennePersister cayennePersister,
            @Inject ICayenneQueryAssembler queryAssembler,
            @Inject Map<String, AgEntityOverlay<?>> entityOverlays) {

        this.cayenneEntityResolver = cayennePersister.entityResolver();
        this.entityOverlays = entityOverlays;

        this.defaultRootResolver = createDefaultRootResolver(queryAssembler, cayennePersister);
        this.defaultRelatedDataResolver = createDefaultRelatedResolver(queryAssembler, cayennePersister);
    }

    @Override
    public <T> AgEntity<T> compile(Class<T> type, AgSchema schema) {

        ObjEntity objEntity = cayenneEntityResolver.getObjEntity(type);
        return objEntity != null
                ? new LazyEntity<>(type, () -> doCompile(type, schema))
                : null;
    }

    protected RootDataResolver<?> createDefaultRootResolver(
            ICayenneQueryAssembler queryAssembler,
            ICayennePersister cayennePersister) {

        return new ViaQueryResolver(queryAssembler, cayennePersister);
    }

    protected RelatedDataResolver<?> createDefaultRelatedResolver(
            ICayenneQueryAssembler queryAssembler,
            ICayennePersister cayennePersister) {

        return new ContextualCayenneRelatedDataResolver<>(
                new ViaQueryWithParentExpResolver(queryAssembler, cayennePersister),
                new ViaQueryWithParentIdsResolver<>(queryAssembler, cayennePersister)
        );
    }

    private <T> AgEntity<T> doCompile(Class<T> type, AgSchema schema) {
        LOGGER.debug("compiling Cayenne entity for type: {}", type);

        return new CayenneAgEntityBuilder<>(type, schema, cayenneEntityResolver)
                .overlays(entityOverlays)
                .dataResolver((RootDataResolver<T>) defaultRootResolver)
                .relatedDataResolver((RelatedDataResolver<T>) defaultRelatedDataResolver)
                .build();
    }
}
