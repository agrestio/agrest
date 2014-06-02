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
 * Unlike Cayenne ObjEntity, ClientEntity scope is usually a single JAX RS
 * resource, or more often - just a single request. I.e. it is built on the fly
 * by the framework or by the application code.
 */
public class ClientEntity<T> {

	private boolean idIncluded;

	private Class<T> type;
	private ObjEntity entity;
	private Collection<String> attributes;

	private String mapByPath;
	private ClientEntity<?> mapBy;
	private Map<String, ClientEntity<?>> relationships;
	private ObjRelationship incoming;
	private Collection<Ordering> orderings;
	private Expression qualifier;
	private Map<String, ClientProperty> extraProperties;

	public ClientEntity(Class<T> type) {
		this.idIncluded = false;
		this.attributes = new ArrayList<>();
		this.relationships = new HashMap<>();
		this.orderings = new ArrayList<>(2);
		this.extraProperties = new HashMap<>();
		this.type = type;
	}

	public ClientEntity(Class<T> type, ObjEntity entity) {
		this(type);
		this.entity = entity;
	}

	public ClientEntity(Class<T> type, ObjRelationship incoming) {
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

	public Map<String, ClientEntity<?>> getRelationships() {
		return relationships;
	}

	public Map<String, ClientProperty> getExtraProperties() {
		return extraProperties;
	}

	public boolean isIdIncluded() {
		return idIncluded;
	}

	public void setIdIncluded(boolean idIncluded) {
		this.idIncluded = idIncluded;
	}

	public ClientEntity<?> getMapBy() {
		return mapBy;
	}

	public void setMapBy(ClientEntity<?> mapBy) {
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
