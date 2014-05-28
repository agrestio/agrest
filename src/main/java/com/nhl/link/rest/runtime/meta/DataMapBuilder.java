package com.nhl.link.rest.runtime.meta;

import org.apache.cayenne.map.DataMap;

/**
 * A helper class to build Cayenne DataMap containing arbitrary application
 * POJOs, such as LDAP "entities", etc. Such DataMap allows LinkRest to obtain
 * metadata for Cayenne and non-Cayenne entities in a uniform way. 
 */
public abstract class DataMapBuilder {

	public static DataMapBuilder newBuilder(String dataMapName) {
		return new RootDataMapBuilder(dataMapName);
	}

	public abstract ObjEntityBuilder addEntity(Class<?> type);

	/**
	 * Adds a bunch of entities that don't require special customization. To
	 * customize entity parameters (e.g. configure Id columns, etc.), add it
	 * individually via {@link #addEntity(Class)} and use returned
	 * {@link ObjEntityBuilder}.
	 */
	public abstract DataMapBuilder addEntities(Class<?> type, Class<?>... moreTypes);

	public abstract DataMap toDataMap();
}
