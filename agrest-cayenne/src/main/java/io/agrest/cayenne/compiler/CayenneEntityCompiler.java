package io.agrest.cayenne.compiler;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.select.CayenneQueryAssembler;
import io.agrest.cayenne.processor.select.ViaQueryResolver;
import io.agrest.cayenne.processor.select.ViaQueryWithParentExpResolver;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.LazyAgEntity;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.runtime.path.IPathDescriptorManager;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @since 1.24
 */
public class CayenneEntityCompiler implements AgEntityCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CayenneEntityCompiler.class);

    private final EntityResolver cayenneEntityResolver;
    private final Map<String, AgEntityOverlay> entityOverlays;
    private final RootDataResolver defaultRootResolver;
    private final NestedDataResolver defaultNestedResolver;

    public CayenneEntityCompiler(
            @Inject ICayennePersister cayennePersister,
            @Inject IPathDescriptorManager pathDescriptorManager,
            @Inject Map<String, AgEntityOverlay> entityOverlays) {

        this.cayenneEntityResolver = cayennePersister.entityResolver();
        this.entityOverlays = entityOverlays;

        CayenneQueryAssembler queryAssembler = new CayenneQueryAssembler(cayenneEntityResolver, pathDescriptorManager);
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
            CayenneQueryAssembler queryAssembler,
            ICayennePersister cayennePersister) {

        return new ViaQueryResolver(queryAssembler, cayennePersister);
    }

    protected NestedDataResolver<?> createDefaultNestedResolver(
            CayenneQueryAssembler queryAssembler,
            ICayennePersister cayennePersister) {

        return new ViaQueryWithParentExpResolver(queryAssembler, cayennePersister);
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
