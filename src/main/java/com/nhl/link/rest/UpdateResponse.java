package com.nhl.link.rest;

import java.util.HashMap;
import java.util.Map;

public class UpdateResponse<T> extends DataResponse<T> {

	private Map<String, Object> values;
	private Map<String, Object> relatedIds;
	private Object id;

	public UpdateResponse(Class<T> type) {
		super(type);

		this.values = new HashMap<>();
		this.relatedIds = new HashMap<>();
	}

	public UpdateResponse<T> withId(Object id) {
		this.id = id;
		return this;
	}

	public boolean hasChanges() {
		return !values.isEmpty() || !relatedIds.isEmpty();
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public Map<String, Object> getRelatedIds() {
		return relatedIds;
	}

	public Object getId() {
		return id;
	}
}
