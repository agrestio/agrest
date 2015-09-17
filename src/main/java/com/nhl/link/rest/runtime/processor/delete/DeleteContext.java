package com.nhl.link.rest.runtime.processor.delete;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.BaseProcessingContext;

import java.util.Map;

/**
 * @since 1.16
 */
public class DeleteContext<T> extends BaseProcessingContext<T> {

	protected Object id;
	protected Map<String, Object> compoundId;
	protected EntityParent<?> parent;
	protected SimpleResponse response;

	public DeleteContext(Class<T> type) {
		super(type);
	}

	public boolean isById() {
		return id != null || compoundId != null;
	}

	public boolean isCompoundId() {
		return compoundId != null;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
		this.compoundId = null;
	}

	public Map<String, Object> getCompoundId() {
		return compoundId;
	}

	public void setCompoundId(Map<String, Object> compoundId) {
		this.id = null;
		this.compoundId = compoundId;
	}

	public EntityParent<?> getParent() {
		return parent;
	}

	public void setParent(EntityParent<?> parent) {
		this.parent = parent;
	}

	public SimpleResponse getResponse() {
		return response;
	}

	public void setResponse(SimpleResponse response) {
		this.response = response;
	}

}
