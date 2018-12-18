package io.agrest;

import javax.ws.rs.core.Response.Status;

import java.util.Map;

/**
 * Represents a parent in a relationship request.
 * 
 * @since 1.4
 */
public class EntityParent<P> {

	private Class<P> type;
	private AgObjectId id;
	private String relationship;

	public EntityParent(Class<P> parentType, Map<String, Object> parentIds, String relationshipFromParent) {

		this(parentType, relationshipFromParent);

		if (parentIds == null) {
			throw new AgException(Status.INTERNAL_SERVER_ERROR, "Related parent ID is missing");
		}

		this.id = new CompoundObjectId(parentIds);
	}

	public EntityParent(Class<P> parentType, Object parentId, String relationshipFromParent) {

		this(parentType, relationshipFromParent);

		if (parentId == null) {
			throw new AgException(Status.INTERNAL_SERVER_ERROR, "Related parent ID is missing");
		}

		this.id = new SimpleObjectId(parentId);
	}

	public EntityParent(Class<P> parentType, String relationshipFromParent) {

		if (parentType == null) {
			throw new AgException(Status.INTERNAL_SERVER_ERROR, "Related parent type is missing");
		}

		if (relationshipFromParent == null) {
			throw new AgException(Status.INTERNAL_SERVER_ERROR, "Related parent relationship is missing");
		}

		this.type = parentType;
		this.relationship = relationshipFromParent;
	}

	public Class<P> getType() {
		return type;
	}

	public AgObjectId getId() {
		return id;
	}

	public String getRelationship() {
		return relationship;
	}
}
