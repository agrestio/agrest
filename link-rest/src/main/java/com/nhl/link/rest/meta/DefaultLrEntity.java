package com.nhl.link.rest.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;

/**
 * @since 1.12
 */
public class DefaultLrEntity<T> implements LrEntity<T> {

	private String name;
	private Class<T> type;

	private Map<String, LrAttribute> ids;
	private Map<String, LrAttribute> attributes;
	private Map<String, LrRelationship> relationships;

	// TODO: ensure name uniquness between all types of properties

	public DefaultLrEntity(Class<T> type) {
		this.type = type;
		this.relationships = new HashMap<>();
		this.attributes = new HashMap<>();
		this.ids = new HashMap<>();
		this.name = type.getSimpleName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public Collection<LrAttribute> getIds() {
		return ids.values();
	}

	@Override
	public LrAttribute getSingleId() {
		if (ids.isEmpty()) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "No id attribute in entity " + name);
		}

		if (ids.size() > 1) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"Unsupported multi-attribute id in entity " + name);
		}

		return ids.values().iterator().next();
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

	public LrRelationship addRelationship(LrRelationship relationship) {
		return relationships.put(relationship.getName(), relationship);
	}

	public LrAttribute addAttribute(LrAttribute attribute) {
		return attributes.put(attribute.getName(), attribute);
	}

	public LrAttribute addId(LrAttribute id) {
		return ids.put(id.getName(), id);
	}
}
