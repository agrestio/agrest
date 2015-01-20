package com.nhl.link.rest.runtime.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;

/**
 * @since 1.12
 */
class CayenneLrEntity<T> implements LrEntity<T> {

	private Class<T> type;
	private ObjEntity objEntity;
	private Map<String, LrAttribute> attributes;
	private Map<String, LrRelationship> relationships;

	CayenneLrEntity(Class<T> type, ObjEntity objEntity) {
		this.type = type;
		this.objEntity = objEntity;
		this.attributes = new HashMap<>();
		this.relationships = new HashMap<>();
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
	public LrAttribute getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public LrRelationship getRelationship(String name) {
		return relationships.get(name);
	}

	@Override
	public Collection<LrAttribute> getAttributes() {
		return attributes.values();
	}

	@Override
	public Collection<LrRelationship> getRelationships() {
		return relationships.values();
	}

	void addAttribute(LrAttribute attribute) {
		attributes.put(attribute.getName(), attribute);
	}

	void addRelationship(LrRelationship relationship) {
		relationships.put(relationship.getName(), relationship);
	}

}
