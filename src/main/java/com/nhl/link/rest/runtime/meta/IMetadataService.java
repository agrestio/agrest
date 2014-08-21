package com.nhl.link.rest.runtime.meta;

import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.Select;

import com.nhl.link.rest.EntityParent;

/**
 * Provides entity metadata in Cayenne format. Although the actual ObjEntities
 * may describe Cayenne entities, as well as POJOs or any other objects.
 */
// Note that IMetadataService operates in terms of
// ObjEntity/Attribute/Relationship, not in terms of ClassDescriptor/Property.
// Presumably this makes it more portable and Cayenne-independent
public interface IMetadataService {

	ObjEntity getObjEntity(Class<?> type);

	ObjEntity getObjEntity(Select<?> select);

	/**
	 * Returns a named relationship for a given object type. If the type is not
	 * supported or there is no matching relationship, an exception is thrown.
	 * 
	 * @since 1.2
	 */
	ObjRelationship getObjRelationship(Class<?> type, String relationship);

	/**
	 * Returns a relationship to child for a given {@link EntityParent}. If the
	 * type is not supported or there is no matching relationship, an exception
	 * is thrown.
	 * 
	 * @since 1.4
	 */
	ObjRelationship getObjRelationship(EntityParent<?> parent);

	/**
	 * @since 1.2
	 */
	Class<?> getType(String entity);
}
