package com.nhl.link.rest.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.12
 */
public class DefaultLrEntity<T> implements LrEntity<T> {

	private String name;
	private Class<T> type;

	private Collection<LrAttribute> ids;
	private Map<String, LrAttribute> attributes;
	private Map<String, LrRelationship> relationships;

	// TODO: ensure name uniquness between all types of properties

	public DefaultLrEntity(Class<T> type) {
		this.type = type;
		this.relationships = new HashMap<>();
		this.attributes = new HashMap<>();
		this.ids = new ArrayList<>();
	}

	@Override
	public String getName() {
		if (name == null) {
			name = type.getSimpleName();
		}

		return name;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public Collection<LrAttribute> getIds() {
		return ids;
	}

	@Override
	public LrRelationship getRelationship(String name) {
		return relationships.get(name);
	}

	@Override
	public LrAttribute getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Collection<LrRelationship> getRelationships() {
		return relationships.values();
	}

	@Override
	public Collection<LrAttribute> getAttributes() {
		return attributes.values();
	}

	public void addRelationship(LrRelationship relationship) {
		relationships.put(relationship.getName(), relationship);
	}

	public void addAttribute(LrAttribute attribute) {
		attributes.put(attribute.getName(), attribute);
	}

	public void addId(LrAttribute id) {
		ids.add(id);
	}
}
