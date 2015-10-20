package com.nhl.link.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.nhl.link.rest.meta.LrEntity;

/**
 * Contains update data of a single object.
 * 
 * @since 1.3
 */
public class EntityUpdate<T> {

	private Map<String, Object> values;
	private Map<String, Set<Object>> relatedIds;
	private Map<String, Object> id;
	private boolean explicitId;
	private Object mergedTo;
	private LrEntity<T> entity;

	public EntityUpdate(LrEntity<T> entity) {
		this.values = new HashMap<>();
		this.relatedIds = new HashMap<>();
		this.entity = entity;
	}
	
	/**
	 * @since 1.19
	 */
	public LrEntity<T> getEntity() {
		return entity;
	}

	public boolean hasChanges() {
		return !values.isEmpty() || !relatedIds.isEmpty();
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public Map<String, Set<Object>> getRelatedIds() {
		return relatedIds;
	}

	public void addRelatedId(String relationshipName, Object value) {

		Set<Object> values = relatedIds.get(relationshipName);
		if (values == null) {
			values = new HashSet<>();
			relatedIds.put(relationshipName, values);
		}

		values.add(value);
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

	/**
	 * Returns an object that was used to merge this update to.
	 * 
	 * @since 1.8
	 */
	public Object getMergedTo() {
		return mergedTo;
	}

	/**
	 * Sets an object that was used to merge this update to.
	 * 
	 * @since 1.8
	 */
	public void setMergedTo(Object mergedTo) {
		this.mergedTo = mergedTo;
	}

}
