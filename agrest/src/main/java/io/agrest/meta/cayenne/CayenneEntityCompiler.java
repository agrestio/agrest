package io.agrest.meta.cayenne;

import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.LazyAgEntity;
import io.agrest.runtime.cayenne.ICayennePersister;
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

    private EntityResolver resolver;
    private Map<String, AgEntityOverlay> entityOverlays;

    public CayenneEntityCompiler(
            @Inject ICayennePersister cayennePersister,
            @Inject Map<String, AgEntityOverlay> entityOverlays) {

        this.resolver = cayennePersister.entityResolver();
        this.entityOverlays = entityOverlays;
    }

    @Override
    public <T> AgEntity<T> compile(Class<T> type, AgDataMap dataMap) {

        ObjEntity objEntity = resolver.getObjEntity(type);
        if (objEntity == null) {
            return null;
        }
        return new LazyAgEntity<>(type, () -> doCompile(type, dataMap));
    }

    private <T> AgEntity<T> doCompile(Class<T> type, AgDataMap dataMap) {
        LOGGER.debug("compiling Cayenne entity for type: {}", type);
        return new CayenneAgEntityBuilder<>(type, dataMap, resolver)
                .overlay(entityOverlays.get(type.getName()))
                .build();
    }
}
