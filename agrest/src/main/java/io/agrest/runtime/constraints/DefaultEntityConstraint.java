package io.agrest.runtime.constraints;

import java.util.Set;

import io.agrest.EntityConstraint;

/**
 * @since 1.6
 */
class DefaultEntityConstraint implements EntityConstraint {

	private String entityName;
	private boolean allowsId;
	private boolean allowsAllAttributes;
	private Set<String> attributes;
	private Set<String> relationships;

	DefaultEntityConstraint(String entityName, boolean allowsId, boolean allowsAllAttributes, Set<String> attributes,
			Set<String> relationships) {
		this.entityName = entityName;
		this.allowsId = allowsId;
		this.attributes = attributes;
		this.relationships = relationships;
		this.allowsAllAttributes = allowsAllAttributes;
	}

	@Override
	public String getEntityName() {
		return entityName;
	}

	@Override
	public boolean allowsId() {
		return allowsId;
	}

	@Override
	public boolean allowsAllAttributes() {
		return allowsAllAttributes;
	}

	@Override
	public boolean allowsAttribute(String name) {
		return attributes.contains(name);
	}

	@Override
	public boolean allowsRelationship(String name) {
		return relationships.contains(name);
	}

}
