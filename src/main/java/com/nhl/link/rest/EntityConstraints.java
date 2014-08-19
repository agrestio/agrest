package com.nhl.link.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.util.CayenneMapEntry;

/**
 * An immutable object that defines read or write constraints on a given entity.
 * Constraints are predefined on the server side and are applied to each
 * request, ensuring a client can't read or write more data than she is allowed
 * to.
 * 
 * @since 1.3
 */
public class EntityConstraints {

	private boolean idIncluded;
	private Collection<String> attributes;
	private Map<String, EntityConstraints> children;
	private Expression qualifier;
	ObjEntity entity;

	EntityConstraints(ObjEntity entity) {

		if (entity == null) {
			throw new NullPointerException("Null entity");
		}

		this.idIncluded = false;
		this.entity = entity;
		this.children = new HashMap<>();

		// using HashSet, as we'll need fast 'contains' calls on attributes
		this.attributes = new HashSet<>();
	}

	public boolean isIdIncluded() {
		return idIncluded;
	}

	public boolean hasAttribute(String name) {
		return attributes.contains(name);
	}

	public EntityConstraints getChild(String name) {
		return children.get(name);
	}

	public boolean hasChild(String name) {
		return children.containsKey(name);
	}

	public Expression getQualifier() {
		return qualifier;
	}

	void excludeAttributes() {
		attributes.clear();
	}

	void excludeChildren() {
		children.clear();
	}

	void attribute(String attribute) {
		this.attributes.add(attribute);
	}

	void attribute(Property<?> attribute) {
		this.attributes.add(attribute.getName());
	}

	void attributes(Property<?>... attributes) {
		if (attributes != null) {
			for (Property<?> p : attributes) {
				this.attributes.add(p.getName());
			}
		}
	}

	void attributes(String... attributes) {
		if (attributes != null) {
			this.attributes.addAll(Arrays.asList(attributes));
		}
	}

	void includeId(boolean include) {
		this.idIncluded = include;
	}

	void includeId() {
		this.idIncluded = true;
	}

	void excludeId() {
		this.idIncluded = false;
	}

	EntityConstraints ensurePath(String path) {

		Iterator<CayenneMapEntry> it = entity.resolvePathComponents(path);
		EntityConstraints c = this;
		while (it.hasNext()) {
			c = ensurePath(c, it.next());
		}

		return c;
	}

	private EntityConstraints ensurePath(EntityConstraints parent, CayenneMapEntry e) {

		if (e instanceof ObjRelationship) {

			ObjRelationship r = (ObjRelationship) e;

			EntityConstraints child = parent.getChild(r.getName());
			if (child == null) {
				child = new EntityConstraints(r.getTargetEntity());
				parent.children.put(r.getName(), child);
			}

			return child;

		} else {
			throw new IllegalArgumentException("Path contains non-relationship component: " + e);
		}
	}

	void and(Expression qualifier) {
		if (this.qualifier == null) {
			this.qualifier = qualifier;
		} else {
			this.qualifier = this.qualifier.andExp(qualifier);
		}
	}

	void or(Expression qualifier) {
		if (this.qualifier == null) {
			this.qualifier = qualifier;
		} else {
			this.qualifier = this.qualifier.orExp(qualifier);
		}
	}

}
