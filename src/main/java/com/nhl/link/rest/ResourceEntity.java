package com.nhl.link.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.util.ToStringBuilder;

/**
 * A metadata object that describes a data structure of a given REST resource.
 * Connected ResourceEntities form a tree-like structure that usually overlays a
 * certain Cayenne mapping subgraph (unless this is a non-persistent entity),
 * filtering and extending its properties to describe the data structure to be
 * returned to the client.
 * <p>
 * ResourceEntity scope is usually a single request. It is built on the fly by
 * the framework or by the application code.
 */
public class ResourceEntity<T> {

	private boolean idIncluded;

	private Class<T> type;
	private ObjEntity cayenneEntity;
	private Collection<String> attributes;
	private Collection<String> defaultProperties;

	private String mapByPath;
	private ResourceEntity<?> mapBy;
	private Map<String, ResourceEntity<?>> children;
	private ObjRelationship incoming;
	private Collection<Ordering> orderings;
	private Expression qualifier;
	private Map<String, EntityProperty> extraProperties;

	public ResourceEntity(Class<T> type) {
		this.idIncluded = false;
		this.attributes = new ArrayList<>();
		this.defaultProperties = new HashSet<>();
		this.children = new HashMap<>();
		this.orderings = new ArrayList<>(2);
		this.extraProperties = new HashMap<>();
		this.type = type;
	}

	public ResourceEntity(Class<T> type, ObjEntity entity) {
		this(type);
		this.cayenneEntity = entity;
	}

	public ResourceEntity(Class<T> type, ObjRelationship incoming) {
		this(type, incoming.getTargetEntity());
		this.incoming = incoming;
	}

	/**
	 * @since 1.1
	 */
	public ObjEntity getCayenneEntity() {
		return cayenneEntity;
	}

	public ObjRelationship getIncoming() {
		return incoming;
	}

	public Expression getQualifier() {
		return qualifier;
	}

	public void andQualifier(Expression qualifier) {
		if (this.qualifier == null) {
			this.qualifier = qualifier;
		} else {
			this.qualifier = this.qualifier.andExp(qualifier);
		}
	}

	public Collection<Ordering> getOrderings() {
		return orderings;
	}

	public Collection<String> getAttributes() {
		return attributes;
	}

	/**
	 * @since 1.5
	 */
	public Collection<String> getDefaultProperties() {
		return defaultProperties;
	}

	/**
	 * @since 1.5
	 */
	public boolean isDefault(String propertyName) {
		return defaultProperties.contains(propertyName);
	}

	public Map<String, ResourceEntity<?>> getChildren() {
		return children;
	}

	/**
	 * @since 1.1
	 */
	public ResourceEntity<?> getChild(String name) {
		return children.get(name);
	}

	public Map<String, EntityProperty> getExtraProperties() {
		return extraProperties;
	}

	public boolean isIdIncluded() {
		return idIncluded;
	}

	public ResourceEntity<T> includeId(boolean include) {
		this.idIncluded = include;
		return this;
	}

	public ResourceEntity<T> includeId() {
		this.idIncluded = true;
		return this;
	}

	public ResourceEntity<T> excludeId() {
		this.idIncluded = false;
		return this;
	}

	public ResourceEntity<?> getMapBy() {
		return mapBy;
	}

	/**
	 * @since 1.1
	 */
	public ResourceEntity<T> mapBy(ResourceEntity<?> mapBy, String mapByPath) {
		this.mapByPath = mapByPath;
		this.mapBy = mapBy;
		return this;
	}

	public String getMapByPath() {
		return mapByPath;
	}

	@Override
	public String toString() {

		ToStringBuilder tsb = new ToStringBuilder(this);
		if (cayenneEntity != null) {
			tsb.append("name", cayenneEntity.getName());
		}

		return tsb.toString();
	}

	public Class<T> getType() {
		return type;
	}
}
