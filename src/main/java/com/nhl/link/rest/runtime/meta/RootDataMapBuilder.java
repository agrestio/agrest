package com.nhl.link.rest.runtime.meta;

import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.map.DataMap;

class RootDataMapBuilder extends DataMapBuilder {

	private Map<Class<?>, ObjEntityBuilder> entityBuilders;
	private DataMap map;

	RootDataMapBuilder(String name) {
		this.map = new DataMap(name);
		this.entityBuilders = new HashMap<>();
	}

	@Override
	public ObjEntityBuilder addEntity(Class<?> type) {
		ObjEntityBuilder builder = entityBuilders.get(type);
		if (builder == null) {
			builder = new ObjEntityBuilder(this, type);
			entityBuilders.put(type, builder);
		}

		return builder;
	}

	/**
	 * Adds a bunch of entities that don't require special customization. To
	 * customize entity parameters (e.g. configure Id columns, etc.), add it
	 * individually via {@link #addEntity(Class)} and use returned
	 * {@link ObjEntityBuilder}.
	 */
	@Override
	public DataMapBuilder addEntities(Class<?> type, Class<?>... moreTypes) {
		addEntity(type);
		if (moreTypes != null) {
			for (Class<?> t : moreTypes) {
				addEntity(t);
			}
		}
		return this;
	}

	@Override
	public DataMap toDataMap() {

		for (ObjEntityBuilder builder : entityBuilders.values()) {
			builder.toEntity();
		}

		return map;
	}

	DataMap getMap() {
		return map;
	}
}
