package io.agrest.meta.compiler;

import io.agrest.AgException;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityBuilder;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.DefaultAgEntity;
import io.agrest.meta.LazyAgEntity;
import io.agrest.property.BeanPropertyReader;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.ParentPropertyDataResolvers;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @since 1.24
 */
// TODO: support for injectable DataResolvers...
public class PojoEntityCompiler implements AgEntityCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PojoEntityCompiler.class);

    private Map<String, AgEntityOverlay> entityOverlays;
    private NestedDataResolver defaultNestedResolver;

    public PojoEntityCompiler(@Inject Map<String, AgEntityOverlay> entityOverlays) {
        this.entityOverlays = entityOverlays;
        this.defaultNestedResolver = createDefaultNestedResolver();
    }

    @Override
    public <T> AgEntity<T> compile(Class<T> type, AgDataMap dataMap) {
        return new LazyAgEntity<>(type, () -> doCompile(type, dataMap));
    }

    protected NestedDataResolver<?> createDefaultNestedResolver() {
        return ParentPropertyDataResolvers.forReaderFactory(e -> BeanPropertyReader.reader(e.getIncoming().getName()));
    }

    private <T> AgEntity<T> doCompile(Class<T> type, AgDataMap dataMap) {
        LOGGER.debug("compiling entity of type {}", type);

        DefaultAgEntity<T> entity = load(type, dataMap);
        checkEntityValid(entity);
        return entity;
    }

    protected <T> DefaultAgEntity<T> load(Class<T> type, AgDataMap dataMap) {
        return new AgEntityBuilder<>(type, dataMap)
                .overlay(entityOverlays.get(type.getName()))
                // while we don't have a notion of a default "root" resolver for POJOs, we have one for nested resolvers
                // - just read values from parent
                .nestedDataResolver(defaultNestedResolver)
                .build();
    }

    protected <T> void checkEntityValid(AgEntity<T> entity) {
        // TODO: what's wrong with an empty entity? It may not be very useful, but still valid
        if (entity.getIds().isEmpty() && entity.getAttributes().isEmpty() && entity.getRelationships().isEmpty()) {
            throw new AgException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Invalid entity '" + entity.getType().getName() + "' - no properties");
        }
    }
}
