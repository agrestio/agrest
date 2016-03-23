package com.nhl.link.rest.runtime.processor.delete;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.BaseProcessingContext;

import java.util.Map;

/**
 * @since 1.16
 */
public class DeleteContext<T> extends BaseProcessingContext<T> {

	protected LrObjectId id;
	protected EntityParent<?> parent;
	protected SimpleResponse response;

	public DeleteContext(Class<T> type) {
		super(type);
	}

	public boolean isById() {
		return id != null;
	}

	public LrObjectId getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = new LrObjectId(id);
	}

	public void setCompoundId(Map<String, Object> ids) {
		this.id = new LrObjectId(ids);
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
