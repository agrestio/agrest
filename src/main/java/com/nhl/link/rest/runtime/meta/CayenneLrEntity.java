package com.nhl.link.rest.runtime.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.meta.LrAttribute;

/**
 * @since 1.12
 */
class CayenneLrEntity<T> implements LrEntity<T> {

	private Class<T> type;
	private ObjEntity objEntity;
	private Map<String, LrAttribute> transientAttributes;
	private Map<String, LrPersistentAttribute> persistentAttributes;
	private Map<String, LrRelationship> relationships;

	// TODO: ensure name uniquness between all types of properties

	CayenneLrEntity(Class<T> type, ObjEntity objEntity) {
		this.type = type;
		this.objEntity = objEntity;
		this.persistentAttributes = new HashMap<>();
		this.relationships = new HashMap<>();
		this.transientAttributes = new HashMap<>();
	}

	@Override
	public String getName() {
		return objEntity.getName();
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public ObjEntity getObjEntity() {
		return objEntity;
	}

	@Override
	public LrPersistentAttribute getPersistentAttribute(String name) {
		return persistentAttributes.get(name);
	}

	@Override
	public LrRelationship getRelationship(String name) {
		return relationships.get(name);
	}

	@Override
	public LrAttribute getTransientAttribute(String name) {
		return transientAttributes.get(name);
	}

	@Override
	public Collection<LrPersistentAttribute> getPersistentAttributes() {
		return persistentAttributes.values();
	}

	@Override
	public Collection<LrRelationship> getRelationships() {
		return relationships.values();
	}

	@Override
	public Collection<LrAttribute> getTransientAttributes() {
		return transientAttributes.values();
	}

	void addPersistentAttribute(LrPersistentAttribute attribute) {
		persistentAttributes.put(attribute.getName(), attribute);
	}

	void addRelationship(LrRelationship relationship) {
		relationships.put(relationship.getName(), relationship);
	}

	void addTransientAttribute(LrAttribute attribute) {
		transientAttributes.put(attribute.getName(), attribute);
	}

}
