package com.nhl.link.rest;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.core.Response.Status;

/**
 * @since 1.7
 */
public class UpdateResponse<T> extends DataResponse<T> {

	private boolean idUpdatesDisallowed;
	private Collection<EntityUpdate<T>> updates;
	private EntityParent<?> parent;
	private boolean includeData;

	public UpdateResponse(Class<T> type) {
		super(type);

		this.updates = new ArrayList<>();
	}

	/**
	 * @since 1.4
	 */
	public UpdateResponse<T> parent(EntityParent<?> parent) {
		this.parent = parent;
		return this;
	}

	public boolean hasChanges() {

		for (EntityUpdate<T> u : updates) {
			if (u.hasChanges()) {
				return true;
			}
		}

		return false;
	}

	public Collection<EntityUpdate<T>> getUpdates() {
		return updates;
	}

	/**
	 * Returns first update object. Throws unless this response contains exactly
	 * one update.
	 */
	public EntityUpdate<T> getFirst() {

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

	public EntityParent<?> getParent() {
		return parent;
	}

	public boolean isIncludeData() {
		return includeData;
	}

	public UpdateResponse<T> includeData() {
		this.includeData = true;
		return this;
	}

	public UpdateResponse<T> excludeData() {
		this.includeData = false;
		return this;
	}
}
