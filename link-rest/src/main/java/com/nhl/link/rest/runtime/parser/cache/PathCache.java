package com.nhl.link.rest.runtime.parser.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.parser.ASTObjPath;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.meta.IMetadataService;

/**
 * @since 1.5
 */
public class PathCache implements IPathCache {

	private IMetadataService metadataService;
	private ConcurrentMap<String, EntityPathCache> pathCacheByEntity;

	public PathCache(@Inject IMetadataService metadataService) {
		this.pathCacheByEntity = new ConcurrentHashMap<String, EntityPathCache>();
		this.metadataService = metadataService;
	}

	@Override
	public PathDescriptor getPathDescriptor(LrEntity<?> entity, ASTObjPath path) {
		return entityPathCache(entity).getPathDescriptor(path);
	}

	EntityPathCache entityPathCache(LrEntity<?> entity) {

		EntityPathCache pathCache = pathCacheByEntity.get(entity.getName());
		if (pathCache != null) {
			return pathCache;
		}

		pathCache = new EntityPathCache(entity, metadataService);
		EntityPathCache previousCache = pathCacheByEntity.putIfAbsent(entity.getName(), pathCache);
		if (previousCache != null) {
			pathCache = previousCache;
		}

		return pathCache;
	}

}
