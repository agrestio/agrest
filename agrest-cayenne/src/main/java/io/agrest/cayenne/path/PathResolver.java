package io.agrest.cayenne.path;

import io.agrest.cayenne.persister.ICayennePersister;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A caching {@link IPathResolver} implementation.
 *
 * @since 5.0
 */
public class PathResolver implements IPathResolver {

    private final EntityResolver entityResolver;
    private final Map<String, EntityPathCache> pathCaches;

    public PathResolver(@Inject ICayennePersister persister) {
        this.entityResolver = persister.entityResolver();
        this.pathCaches = new ConcurrentHashMap<>();
    }

    @Override
    public PathDescriptor resolve(String entityName, String agPath) {
        return entityPathCache(entityName).getOrCreate(agPath);
    }

    EntityPathCache entityPathCache(String entityName) {
        return pathCaches.computeIfAbsent(
                entityName,
                k -> new EntityPathCache(entityResolver.getObjEntity(k)));
    }

}
