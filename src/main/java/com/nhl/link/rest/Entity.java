package com.nhl.link.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.util.ToStringBuilder;

/**
 * A facade to Cayenne ObjEntity that defines the format of a data structure
 * that should be sent to the client via LinkRest. Connected ClientEntities form
 * a tree-like structure that overlays a certain Cayenne mapping subgraph,
 * filtering and extending its properties to describe the data structure to be
 * returned to the client.
 * <p>
 * ClientEntity scope is usually a single request. It is built on the fly by the
 * framework or by the application code.
 */
public class Entity<T> {

	private boolean idIncluded;

	private Class<T> type;
	private ObjEntity entity;
	private Collection<String> attributes;

	private String mapByPath;
	private Entity<?> mapBy;
	private Map<String, Entity<?>> relationships;
	private ObjRelationship incoming;
	private Collection<Ordering> orderings;
	private Expression qualifier;
	private Map<String, EntityProperty> extraProperties;

	public Entity(Class<T> type) {
		this.idIncluded = false;
		this.attributes = new ArrayList<>();
		this.relationships = new HashMap<>();
		this.orderings = new ArrayList<>(2);
		this.extraProperties = new HashMap<>();
		this.type = type;
	}

	public Entity(Class<T> type, ObjEntity entity) {
		this(type);
		this.entity = entity;
	}

	public Entity(Class<T> type, ObjRelationship incoming) {
		this(type, incoming.getTargetEntity());
		this.incoming = incoming;
	}

	public ObjEntity getEntity() {
		return entity;
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

	public Map<String, Entity<?>> getRelationships() {
		return relationships;
	}

	public Map<String, EntityProperty> getExtraProperties() {
		return extraProperties;
	}

	public boolean isIdIncluded() {
		return idIncluded;
	}

	public void setIdIncluded(boolean idIncluded) {
		this.idIncluded = idIncluded;
	}

	public Entity<?> getMapBy() {
		return mapBy;
	}

	public void setMapBy(Entity<?> mapBy) {
		this.mapBy = mapBy;
	}

	public String getMapByPath() {
		return mapByPath;
	}

	public void setMapByPath(String mapByPath) {
		this.mapByPath = mapByPath;
	}

	@Override
	public String toString() {

		ToStringBuilder tsb = new ToStringBuilder(this);
		if (entity != null) {
			tsb.append("name", entity.getName());
		}

		return tsb.toString();
	}

	public Class<T> getType() {
		return type;
	}
}
