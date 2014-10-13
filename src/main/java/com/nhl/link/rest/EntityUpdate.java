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
	private Map<String, Object> id;
	private boolean explicitId;

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

	/**
	 * @since 1.8
	 */
	public Map<String, Object> getId() {
		return id;
	}

	/**
	 * @since 1.8
	 */
	public Map<String, Object> getOrCreateId() {
		if (id == null) {
			id = new HashMap<>();
		}

		return id;
	}

	/**
	 * @since 1.8
	 */
	public void setExplicitId() {
		this.explicitId = true;
	}

	/**
	 * @since 1.5
	 */
	public boolean isExplicitId() {
		return explicitId;
	}

}
