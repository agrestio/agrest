package io.agrest.meta.compiler;

import io.agrest.AgRESTException;
import io.agrest.meta.AgEntity;
import io.agrest.meta.DefaultAgEntity;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntityBuilder;
import io.agrest.meta.AgEntityOverlay;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @since 1.24
 */
public class PojoEntityCompiler implements AgEntityCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PojoEntityCompiler.class);

    private Map<String, AgEntityOverlay> entityOverlays;

    public PojoEntityCompiler(@Inject Map<String, AgEntityOverlay> entityOverlays) {
        this.entityOverlays = entityOverlays;
    }

    @Override
    public <T> AgEntity<T> compile(Class<T> type, AgDataMap dataMap) {
        return new LazyAgEntity<>(type, () -> doCompile(type, dataMap));
    }

    private <T> AgEntity<T> doCompile(Class<T> type, AgDataMap dataMap) {
        LOGGER.debug("compiling entity of type {}", type);

        DefaultAgEntity<T> entity = loadAnnotatedProperties(type, dataMap);
        loadOverlays(entity, dataMap);
        checkEntityValid(entity);
        return entity;
    }

    protected <T> DefaultAgEntity<T> loadAnnotatedProperties(Class<T> type, AgDataMap dataMap) {
        return new AgEntityBuilder<>(type, dataMap).build();
    }

    protected <T> void loadOverlays(DefaultAgEntity<T> entity, AgDataMap dataMap) {
        AgEntityOverlay<?> overlay = entityOverlays.get(entity.getType().getName());
        if (overlay != null) {
            overlay.getAttributes().forEach(entity::addAttribute);
            overlay.getRelatonships(dataMap).forEach(entity::addRelationship);
        }
    }

    protected <T> void checkEntityValid(AgEntity<T> entity) {
        if (entity.getIds().isEmpty() && entity.getAttributes().isEmpty() && entity.getRelationships().isEmpty()) {
            throw new AgRESTException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Invalid entity '" + entity.getType().getName() + "' - no properties");
        }
    }
}
