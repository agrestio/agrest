package com.nhl.link.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains update data of a single object.
 * 
 * @since 1.3
 */
public class EntityUpdate {

	private Map<String, Object> values;
	private Map<String, Object> relatedIds;
	private Object id;

	public EntityUpdate() {
		this.values = new HashMap<>();
		this.relatedIds = new HashMap<>();
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

	public void setId(Object id) {
		this.id = id;
	}

}
