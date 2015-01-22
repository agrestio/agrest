package com.nhl.link.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.util.CayenneMapEntry;

import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;

/**
 * An immutable snapshot of {@link ConstraintsBuilder}.
 * 
 * @since 1.3
 */
public class ImmutableTreeConstraints {

	private boolean idIncluded;
	private Collection<String> attributes;
	private Map<String, ImmutableTreeConstraints> children;
	private Expression qualifier;
	LrEntity<?> entity;

	ImmutableTreeConstraints(LrEntity<?> entity) {

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

	public ImmutableTreeConstraints getChild(String name) {
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

	void allAttributes() {
		for (LrAttribute a : entity.getPersistentAttributes()) {
			this.attributes.add(a.getName());
		}

		for (LrAttribute a : entity.getTransientAttributes()) {
			this.attributes.add(a.getName());
		}
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

	ImmutableTreeConstraints ensurePath(String path) {

		Iterator<CayenneMapEntry> it = entity.resolvePathComponents(path);
		ImmutableTreeConstraints c = this;
		while (it.hasNext()) {
			c = ensurePath(c, it.next());
		}

		return c;
	}

	private ImmutableTreeConstraints ensurePath(ImmutableTreeConstraints parent, Object pathComponent) {

		if (pathComponent instanceof LrRelationship) {

			LrRelationship r = (LrRelationship) pathComponent;

			ImmutableTreeConstraints child = parent.getChild(r.getName());
			if (child == null) {
				child = new ImmutableTreeConstraints(r.getTargetEntity());
				parent.children.put(r.getName(), child);
			}

			return child;

		} else {
			throw new IllegalArgumentException("Path contains non-relationship component: " + pathComponent);
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
