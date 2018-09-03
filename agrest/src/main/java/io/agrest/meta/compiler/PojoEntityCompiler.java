package io.agrest.meta.compiler;

import io.agrest.LinkRestException;
import io.agrest.meta.DefaultLrEntity;
import io.agrest.meta.LrDataMap;
import io.agrest.meta.LrEntity;
import io.agrest.meta.LrEntityBuilder;
import io.agrest.meta.LrEntityOverlay;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @since 1.24
 */
public class PojoEntityCompiler implements LrEntityCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PojoEntityCompiler.class);

    private Map<String, LrEntityOverlay> entityOverlays;

    public PojoEntityCompiler(@Inject Map<String, LrEntityOverlay> entityOverlays) {
        this.entityOverlays = entityOverlays;
    }

    @Override
    public <T> LrEntity<T> compile(Class<T> type, LrDataMap dataMap) {
        return new LazyLrEntity<>(type, () -> doCompile(type, dataMap));
    }

    private <T> LrEntity<T> doCompile(Class<T> type, LrDataMap dataMap) {
        LOGGER.debug("compiling entity of type {}", type);

        DefaultLrEntity<T> entity = loadAnnotatedProperties(type, dataMap);
        loadOverlays(entity, dataMap);
        checkEntityValid(entity);
        return entity;
    }

    protected <T> DefaultLrEntity<T> loadAnnotatedProperties(Class<T> type, LrDataMap dataMap) {
        return new LrEntityBuilder<>(type, dataMap).build();
    }

    protected <T> void loadOverlays(DefaultLrEntity<T> entity, LrDataMap dataMap) {
        LrEntityOverlay<?> overlay = entityOverlays.get(entity.getType().getName());
        if (overlay != null) {
            overlay.getAttributes().forEach(entity::addAttribute);
            overlay.getRelatonships(dataMap).forEach(entity::addRelationship);
        }
    }

    protected <T> void checkEntityValid(LrEntity<T> entity) {
        if (entity.getIds().isEmpty() && entity.getAttributes().isEmpty() && entity.getRelationships().isEmpty()) {
            throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Invalid entity '" + entity.getType().getName() + "' - no properties");
        }
    }
}
