package io.agrest.cayenne.path;

import io.agrest.meta.AgEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A caching {@link IPathResolver} implementation.
 *
 * @since 5.0
 */
public class PathResolver implements IPathResolver {

    private final Map<String, EntityPathCache> pathCaches;

    public PathResolver() {
        this.pathCaches = new ConcurrentHashMap<>();
    }

    @Override
    public PathDescriptor resolve(AgEntity<?> entity, String agPath) {
        return entityPathCache(entity).getOrCreate(agPath);
    }

    EntityPathCache entityPathCache(AgEntity<?> entity) {
        return pathCaches.computeIfAbsent(
                entity.getName(),
                k -> new EntityPathCache(entity));
    }

}
