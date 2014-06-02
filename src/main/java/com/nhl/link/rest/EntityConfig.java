package com.nhl.link.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.util.CayenneMapEntry;

/**
 * Stores parameters pertaining to the {@link Entity} configuration in a
 * {@link DataResponse}.
 * 
 * @since 1.1
 */
public class EntityConfig {

	private boolean idIncluded;
	private Collection<String> attributes;
	private ConcurrentMap<String, EntityConfig> children;
	private Expression qualifier;
	private ObjEntity entity;

	public EntityConfig(ObjEntity entity) {
		this.idIncluded = false;
		this.entity = entity;
		this.children = new ConcurrentHashMap<>();

		// using HashSet, as we'll need fast 'contains' calls on attributes
		this.attributes = new HashSet<>();
	}

	/**
	 * Excludes all previously included attribites.
	 */
	public EntityConfig excludeAttributes() {
		attributes.clear();
		return this;
	}

	/**
	 * Excludes all previously included child configs.
	 */
	public EntityConfig excludeChildren() {
		children.clear();
		return this;
	}

	public EntityConfig attribute(String attribute) {
		this.attributes.add(attribute);
		return this;
	}

	public EntityConfig attribute(Property<?> attribute) {
		this.attributes.add(attribute.getName());
		return this;
	}

	public EntityConfig attributes(Property<?>... attributes) {
		if (attributes != null) {
			for (Property<?> p : attributes) {
				this.attributes.add(p.getName());
			}
		}
		return this;
	}

	public EntityConfig attributes(String... attributes) {
		if (attributes != null) {
			this.attributes.addAll(Arrays.asList(attributes));
		}
		return this;
	}

	public EntityConfig path(Property<?> path) {
		return path(path.getName());
	}
	
	public EntityConfig path(String path) {
		return path(entity.resolvePathComponents(path));
	}

	private EntityConfig path(Iterator<CayenneMapEntry> it) {

		if (it.hasNext()) {
			CayenneMapEntry entry = it.next();

			if (entry instanceof ObjRelationship) {
				EntityConfig child = getOrMakeChild(entry.getName());
				return child.path(it);
			} else {
				attribute(entry.getName());
			}
		}

		return this;
	}

	public EntityConfig getOrMakeChild(String name) {
		EntityConfig child = children.get(name);
		if (child == null) {

			ObjRelationship r = entity.getRelationship(name);
			if (r == null) {
				throw new IllegalArgumentException(name + " is not a valid relationship in " + entity.getName());
			}

			EntityConfig newChild = new EntityConfig(r.getTargetEntity());
			EntityConfig oldChild = children.putIfAbsent(name, newChild);
			child = oldChild != null ? oldChild : newChild;
		}

		return child;
	}

	public EntityConfig includeId() {
		this.idIncluded = true;
		return this;
	}

	public EntityConfig excludeId() {
		this.idIncluded = false;
		return this;
	}

	public EntityConfig andQualifier(Expression qualifier) {
		if (this.qualifier == null) {
			this.qualifier = qualifier;
		} else {
			this.qualifier = this.qualifier.andExp(qualifier);
		}

		return this;
	}

	public boolean isIdIncluded() {
		return idIncluded;
	}

	public boolean hasAttribute(String name) {
		return attributes.contains(name);
	}

	public Collection<String> getAttributes() {
		return attributes;
	}

	public Map<String, EntityConfig> getChildren() {
		return children;
	}

	public EntityConfig getChild(String name) {
		return children.get(name);
	}

	public boolean hasChild(String name) {
		return children.containsKey(name);
	}

	public Expression getQualifier() {
		return qualifier;
	}

}
