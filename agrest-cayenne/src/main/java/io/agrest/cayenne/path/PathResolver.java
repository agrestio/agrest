package io.agrest.cayenne.path;

import io.agrest.meta.AgEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A caching {@link IPathResolver} implementation. Each app only has a fixed number of paths, so this makes
 * {@link PathDescriptor} lookup efficient.
 *
 * @since 5.0
 */
public class PathResolver implements IPathResolver {

    private Map<String, EntityPathCache> pathCacheByEntity;

    public PathResolver() {
        this.pathCacheByEntity = new ConcurrentHashMap<>();
    }

    @Override
    public PathDescriptor resolve(AgEntity<?> entity, String agPath) {
        return entityPathCache(entity).getOrCreate(agPath);
    }

    EntityPathCache entityPathCache(AgEntity<?> entity) {
        return pathCacheByEntity.computeIfAbsent(entity.getName(), k -> new EntityPathCache(entity));
    }

}
