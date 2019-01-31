package io.agrest.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.12
 */
public class DefaultAgEntity<T> implements AgEntity<T> {

	private String name;
	private Class<T> type;

	private Map<String, AgAttribute> ids;
	private Map<String, AgAttribute> attributes;
	private Map<String, AgRelationship> relationships;

	// TODO: ensure name uniquness between all types of properties

	public DefaultAgEntity(Class<T> type) {
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
	public Collection<AgAttribute> getIds() {
		return ids.values();
	}

	@Override
	public AgRelationship getRelationship(String name) {
		return relationships.get(name);
	}

	@Override
	public AgAttribute getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Collection<AgRelationship> getRelationships() {
		return relationships.values();
	}

	@Override
	public AgRelationship getRelationship(AgEntity entity) {
		if (relationships.isEmpty()) {
			return null;
		}

		return relationships.values()
				.stream()
				.filter(r -> r.getTargetEntity().getName().equalsIgnoreCase(entity.getName()))
				.findFirst()
				.get();
	}

	@Override
	public Collection<AgAttribute> getAttributes() {
		return attributes.values();
	}

	public AgRelationship addRelationship(AgRelationship relationship) {
		return relationships.put(relationship.getName(), relationship);
	}

	public AgAttribute addAttribute(AgAttribute attribute) {
		return attributes.put(attribute.getName(), attribute);
	}

	public AgAttribute addId(AgAttribute id) {
		return ids.put(id.getName(), id);
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName()).append("@")
				.append(Integer.toHexString(System.identityHashCode(this))).append("[").append(getName()).append("]")
				.toString();
	}
}
