package io.agrest.compiler;

import io.agrest.meta.*;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @since 4.1
 */
public class AnnotationBasedCompiler implements AgEntityCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationBasedCompiler.class);

    private Map<String, AgEntityOverlay> entityOverlays;

    public AnnotationBasedCompiler(@Inject Map<String, AgEntityOverlay> entityOverlays) {
        this.entityOverlays = entityOverlays;
    }

    @Override
    public <T> AgEntity<T> compile(Class<T> type, AgDataMap dataMap) {
        return new LazyAgEntity<>(type, () -> doCompile(type, dataMap));
    }

    private <T> AgEntity<T> doCompile(Class<T> type, AgDataMap dataMap) {
        LOGGER.debug("compiling entity of type {}", type);
        AgEntity<T> entity = new AgEntityBuilder<>(type, dataMap)
                .overlay(entityOverlays.get(type.getName()))
                .build();

        if (LOGGER.isInfoEnabled()) {
            warnOfEmptyEntity(entity);
        }

        return entity;
    }

    protected <T> void warnOfEmptyEntity(AgEntity<T> entity) {
        if (entity.getIdParts().isEmpty()
                && entity.getAttributes().isEmpty()
                && entity.getRelationships().isEmpty()) {
            LOGGER.info("Empty entity '{}'", entity.getName());
        }
    }
}
