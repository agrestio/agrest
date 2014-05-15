package com.nhl.link.rest.runtime.parser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cayenne.map.ObjEntity;

class PathCache {

	private ConcurrentMap<String, EntityPathCache> pathCacheByEntity;

	PathCache() {
		this.pathCacheByEntity = new ConcurrentHashMap<String, EntityPathCache>();
	}

	EntityPathCache entityPathCache(ObjEntity entity) {

		EntityPathCache pathCache = pathCacheByEntity.get(entity.getName());
		if (pathCache != null) {
			return pathCache;
		}

		pathCache = new EntityPathCache(entity);
		EntityPathCache previousCache = pathCacheByEntity.putIfAbsent(entity.getName(), pathCache);
		if (previousCache != null) {
			pathCache = previousCache;
		}

		return pathCache;
	}

}
