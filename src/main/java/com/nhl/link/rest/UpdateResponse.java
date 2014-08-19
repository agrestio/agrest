package com.nhl.link.rest;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.core.Response.Status;

public class UpdateResponse<T> extends DataResponse<T> {

	private boolean idUpdatesDisallowed;
	private Collection<EntityUpdate> updates;

	public UpdateResponse(Class<T> type) {
		super(type);

		this.updates = new ArrayList<>();
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
}
