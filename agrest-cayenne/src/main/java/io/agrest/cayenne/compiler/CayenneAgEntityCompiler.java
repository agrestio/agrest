package io.agrest.cayenne.compiler;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.processor.select.ContextualCayenneNestedDataResolver;
import io.agrest.cayenne.processor.select.ViaQueryResolver;
import io.agrest.cayenne.processor.select.ViaQueryWithParentExpResolver;
import io.agrest.cayenne.processor.select.ViaQueryWithParentIdsResolver;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.LazyAgEntity;
import io.agrest.resolver.NestedDataResolver;
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
    private final Map<String, AgEntityOverlay> entityOverlays;
    private final RootDataResolver defaultRootResolver;
    private final NestedDataResolver defaultNestedResolver;

    public CayenneAgEntityCompiler(
            @Inject ICayennePersister cayennePersister,
            @Inject ICayenneQueryAssembler queryAssembler,
            @Inject Map<String, AgEntityOverlay> entityOverlays) {

        this.cayenneEntityResolver = cayennePersister.entityResolver();
        this.entityOverlays = entityOverlays;

        this.defaultRootResolver = createDefaultRootResolver(queryAssembler, cayennePersister);
        this.defaultNestedResolver = createDefaultNestedResolver(queryAssembler, cayennePersister);
    }

    @Override
    public <T> AgEntity<T> compile(Class<T> type, AgDataMap dataMap) {

        ObjEntity objEntity = cayenneEntityResolver.getObjEntity(type);
        return objEntity != null
                ? new LazyAgEntity<>(type, () -> doCompile(type, dataMap))
                : null;
    }

    protected RootDataResolver<?> createDefaultRootResolver(
            ICayenneQueryAssembler queryAssembler,
            ICayennePersister cayennePersister) {

        return new ViaQueryResolver(queryAssembler, cayennePersister);
    }

    protected NestedDataResolver<?> createDefaultNestedResolver(
            ICayenneQueryAssembler queryAssembler,
            ICayennePersister cayennePersister) {

        return new ContextualCayenneNestedDataResolver<>(
                new ViaQueryWithParentExpResolver(queryAssembler, cayennePersister),
                new ViaQueryWithParentIdsResolver<>(queryAssembler, cayennePersister)
        );
    }

    private <T> AgEntity<T> doCompile(Class<T> type, AgDataMap dataMap) {
        LOGGER.debug("compiling Cayenne entity for type: {}", type);
        return new CayenneAgEntityBuilder<>(type, dataMap, cayenneEntityResolver)
                .overlay(entityOverlays.get(type.getName()))
                .rootDataResolver(defaultRootResolver)
                .nestedDataResolver(defaultNestedResolver)
                .build();
    }
}
