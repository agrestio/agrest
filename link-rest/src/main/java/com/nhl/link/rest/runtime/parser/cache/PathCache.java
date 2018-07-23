package com.nhl.link.rest.runtime.parser.cache;

import com.nhl.link.rest.meta.LrEntity;
import org.apache.cayenne.exp.parser.ASTObjPath;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 1.5
 */
public class PathCache implements IPathCache {

	private Map<String, EntityPathCache> pathCacheByEntity;

	public PathCache() {
		this.pathCacheByEntity = new ConcurrentHashMap<>();
	}

	@Override
	public PathDescriptor getPathDescriptor(LrEntity<?> entity, ASTObjPath path) {
		return entityPathCache(entity).getPathDescriptor(path);
	}

	EntityPathCache entityPathCache(LrEntity<?> entity) {
		return pathCacheByEntity.computeIfAbsent(entity.getName(), k -> new EntityPathCache(entity));
	}

}
