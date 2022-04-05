package io.agrest.jpa.compiler;

import java.util.Map;

import io.agrest.compiler.AgEntityCompiler;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.select.ViaQueryResolver;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.LazyAgEntity;
import io.agrest.resolver.RootDataResolver;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.apache.cayenne.di.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.0
 */
public class JpaAgEntityCompiler implements AgEntityCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaAgEntityCompiler.class);

    private final Metamodel metamodel;
    private final Map<String, AgEntityOverlay<?>> entityOverlays;
    private final RootDataResolver defaultRootResolver;

    public JpaAgEntityCompiler(
            @Inject IAgJpaPersister jpaPersister,
            @Inject Map<String, AgEntityOverlay<?>> entityOverlays,
            @Inject IJpaQueryAssembler queryAssembler) {
        this.metamodel = jpaPersister.metamodel();
        this.entityOverlays = entityOverlays;
        this.defaultRootResolver = createDefaultRootResolver(queryAssembler);
    }

    @Override
    public <T> AgEntity<T> compile(Class<T> type, AgDataMap dataMap) {
        EntityType<T> entity = metamodel.entity(type);
        return entity != null
                ? new LazyAgEntity<>(type, () -> doCompile(type, dataMap))
                : null;
    }

    protected RootDataResolver<?> createDefaultRootResolver(IJpaQueryAssembler queryAssembler) {
        return new ViaQueryResolver<>(queryAssembler);
    }

    @SuppressWarnings("unchecked")
    protected <T> AgEntity<T> doCompile(Class<T> type, AgDataMap dataMap) {
        LOGGER.debug("compiling Hibernate entity for type: {}", type);
        return new JpaAgEntityBuilder<>(type, dataMap, metamodel)
                .overlay((AgEntityOverlay<T>) entityOverlays.get(type.getName()))
                .rootDataResolver(defaultRootResolver)
                .nestedDataResolver(null)  // FIXME
                .build();
    }
}
