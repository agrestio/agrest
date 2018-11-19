package io.agrest.runtime.path;

import io.agrest.meta.AgEntity;
import io.agrest.backend.exp.parser.ASTObjPath;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link IPathDescriptorManager} that parses expressions on first access and caches them locally. Each app only
 * has a fixed number of paths, so this makes {@link PathDescriptor} lookup efficient.
 */
public class PathDescriptorManager implements IPathDescriptorManager {

    private Map<String, EntityPathCache> pathCacheByEntity;

    public PathDescriptorManager() {
        this.pathCacheByEntity = new ConcurrentHashMap<>();
    }

    @Override
    public PathDescriptor getPathDescriptor(AgEntity<?> entity, ASTObjPath path) {
        return entityPathCache(entity).getPathDescriptor(path);
    }

    EntityPathCache entityPathCache(AgEntity<?> entity) {
        return pathCacheByEntity.computeIfAbsent(entity.getName(), k -> new EntityPathCache(entity));
    }

}
