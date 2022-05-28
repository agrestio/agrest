package io.agrest.compiler;

import io.agrest.meta.*;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @since 4.1
 */
public class AnnotationsAgEntityCompiler implements AgEntityCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsAgEntityCompiler.class);

    private Map<String, AgEntityOverlay> overlays;

    public AnnotationsAgEntityCompiler(@Inject Map<String, AgEntityOverlay> overlays) {
        this.overlays = overlays;
    }

    @Override
    public <T> AgEntity<T> compile(Class<T> type, AgSchema schema) {
        return new LazyAgEntity<>(type, () -> doCompile(type, schema));
    }

    private <T> AgEntity<T> doCompile(Class<T> type, AgSchema schema) {
        LOGGER.debug("compiling entity of type {}", type);
        AgEntity<T> entity = new AnnotationsAgEntityBuilder<>(type, schema)
                .overlay(overlays.get(type.getName()))
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
