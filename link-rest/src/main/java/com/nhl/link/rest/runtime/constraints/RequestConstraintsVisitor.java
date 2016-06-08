package com.nhl.link.rest.runtime.constraints;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.cayenne.exp.Expression;

import com.nhl.link.rest.constraints.ConstraintVisitor;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.parser.PathConstants;

/**
 * An immutable snapshot of {@link ConstraintsBuilder}.
 * 
 * @since 1.3
 */
public class RequestConstraintsVisitor implements ConstraintVisitor {

	private boolean idIncluded;
	private Collection<String> attributes;
	private Map<String, RequestConstraintsVisitor> children;
	private Expression qualifier;
	private LrEntity<?> entity;

	RequestConstraintsVisitor(LrEntity<?> entity) {

		if (entity == null) {
			throw new NullPointerException("Null entity");
		}

		this.idIncluded = false;
		this.entity = entity;
		this.children = new HashMap<>();

		// using HashSet, as we'll need fast 'contains' calls on attributes
		this.attributes = new HashSet<>();
	}

	Collection<String> getAttributes() {
		return attributes;
	}

	Map<String, RequestConstraintsVisitor> getChildren() {
		return children;
	}

	public boolean isIdIncluded() {
		return idIncluded;
	}

	public boolean hasAttribute(String name) {
		return attributes.contains(name);
	}

	public RequestConstraintsVisitor getChild(String name) {
		return children.get(name);
	}

	public boolean hasChild(String name) {
		return children.containsKey(name);
	}

	public Expression getQualifier() {
		return qualifier;
	}

	@Override
	public void visitExcludePropertiesConstraint(String... attributesOrRelationships) {
		if (attributesOrRelationships != null) {
			for (String name : attributesOrRelationships) {

				if (!attributes.remove(name)) {
					children.remove(name);
				}
			}
		}
	}

	@Override
	public void visitExcludeAllAttributesConstraint() {
		attributes.clear();
	}

	@Override
	public void visitExcludeAllChildrenConstraint() {
		children.clear();
	}

	@Override
	public void visitIncludeAttributesConstraint(String... attributes) {
		if (attributes != null) {

			for (String a : attributes) {
				this.attributes.add(a);
			}
		}
	}

	@Override
	public void visitIncludeAllAttributesConstraint() {
		for (LrAttribute a : entity.getAttributes()) {
			this.attributes.add(a.getName());
		}
	}

	@Override
	public void visitIncludeIdConstraint(boolean include) {
		this.idIncluded = include;
	}

	@Override
	public void visitAndQualifierConstraint(Expression qualifier) {
		if (this.qualifier == null) {
			this.qualifier = qualifier;
		} else {
			this.qualifier = this.qualifier.andExp(qualifier);
		}
	}

	@Override
	public void visitOrQualifierConstraint(Expression qualifier) {

		if (this.qualifier == null) {
			this.qualifier = qualifier;
		} else {
			this.qualifier = this.qualifier.orExp(qualifier);
		}
	}

	@Override
	public ConstraintVisitor subtreeVisitor(String path) {

		StringTokenizer segments = new StringTokenizer(path, Character.toString(PathConstants.DOT));

		RequestConstraintsVisitor c = this;
		while (segments.hasMoreTokens()) {
			c = ensurePath(c, segments.nextToken());
		}

		return c;
	}

	private RequestConstraintsVisitor ensurePath(RequestConstraintsVisitor parent, String pathSegment) {

		LrRelationship relationship = parent.entity.getRelationship(pathSegment);

		if (relationship == null) {
			throw new IllegalArgumentException("Path contains non-relationship component: " + pathSegment);
		}

		RequestConstraintsVisitor child = parent.getChild(relationship.getName());
		if (child == null) {
			LrEntity<?> targetEntity = relationship.getTargetEntity();
			child = new RequestConstraintsVisitor(targetEntity);
			parent.children.put(relationship.getName(), child);
		}

		return child;
	}

}
