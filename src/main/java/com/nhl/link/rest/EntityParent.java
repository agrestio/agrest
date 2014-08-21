package com.nhl.link.rest;

import javax.ws.rs.core.Response.Status;

/**
 * Represents a parent in a relationship request.
 * 
 * @since 1.4
 */
public class EntityParent<P> {

	private Class<P> type;
	private Object id;
	private String relationship;

	public EntityParent(Class<P> parentType, Object parentId, String relationshipFromParent) {

		if (parentType == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Related parent type is missing");
		}

		if (parentId == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Related parent ID is missing");
		}

		if (relationshipFromParent == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Related parent relationship is missing");
		}

		this.id = parentId;
		this.type = parentType;
		this.relationship = relationshipFromParent;
	}

	public Class<P> getType() {
		return type;
	}

	public Object getId() {
		return id;
	}

	public String getRelationship() {
		return relationship;
	}
}
