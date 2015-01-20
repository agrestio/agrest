package com.nhl.link.rest.meta;

import java.util.Collection;

import org.apache.cayenne.map.ObjEntity;

/**
 * An entity model shared by LinkRest stack.
 * 
 * @since 1.12
 */
public interface LrEntity<T> {

	String getName();

	Class<T> getType();

	ObjEntity getObjEntity();

	LrAttribute getAttribute(String name);

	LrRelationship getRelationship(String name);

	Collection<LrAttribute> getAttributes();

	Collection<LrRelationship> getRelationships();

}
