package com.nhl.link.rest;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.core.Response.Status;

public class UpdateResponse<T> extends DataResponse<T> {

	private boolean idUpdatesDisallowed;
	private Collection<EntityUpdate> updates;

	private Class<?> parentType;
	private Object parentId;
	private String relationshipFromParent;

	public UpdateResponse(Class<T> type) {
		super(type);

		this.updates = new ArrayList<>();
	}

	/**
	 * @since 1.4
	 */
	public UpdateResponse<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		this.parentId = parentId;
		this.parentType = parentType;
		this.relationshipFromParent = relationshipFromParent;
		return this;
	}

	public boolean hasChanges() {

		for (EntityUpdate u : updates) {
			if (u.hasChanges()) {
				return true;
			}
		}

		return false;
	}

	public Collection<EntityUpdate> getUpdates() {
		return updates;
	}

	/**
	 * Returns first update object. Throws unless this response contains exactly
	 * one update.
	 */
	public EntityUpdate getFirst() {

		if (updates.size() != 1) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Expected one object in update. Actual: "
					+ updates.size());
		}

		return updates.iterator().next();
	}

	public boolean idUpdatesDisallowed() {
		return idUpdatesDisallowed;
	}

	public UpdateResponse<T> disallowIdUpdates() {
		this.idUpdatesDisallowed = true;
		return this;
	}

	public boolean isIdUpdatesDisallowed() {
		return idUpdatesDisallowed;
	}

	/**
	 * @since 1.4
	 */
	public Class<?> getParentType() {
		return parentType;
	}

	/**
	 * @since 1.4
	 */
	public Object getParentId() {
		return parentId;
	}

	/**
	 * @since 1.4
	 */
	public String getRelationshipFromParent() {
		return relationshipFromParent;
	}

}
