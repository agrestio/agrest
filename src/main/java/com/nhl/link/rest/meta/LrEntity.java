package com.nhl.link.rest.meta;

import java.util.Collection;

import org.apache.cayenne.map.ObjEntity;

/**
 * An entity model shared across LinkRest stack.
 * 
 * @since 1.12
 */
public interface LrEntity<T> {

	String getName();

	Class<T> getType();

	ObjEntity getObjEntity();

	LrPersistentAttribute getPersistentAttribute(String name);

	LrRelationship getRelationship(String name);

	LrAttribute getTransientAttribute(String name);

	Collection<LrPersistentAttribute> getPersistentAttributes();

	Collection<LrRelationship> getRelationships();
	
	Collection<LrAttribute> getTransientAttributes();

}
