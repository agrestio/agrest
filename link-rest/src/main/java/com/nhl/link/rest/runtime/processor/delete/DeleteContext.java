package com.nhl.link.rest.runtime.processor.delete;

import java.util.Map;

import com.nhl.link.rest.CompoundObjectId;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.SimpleObjectId;
import com.nhl.link.rest.processor.BaseProcessingContext;

/**
 * @since 1.16
 */
public class DeleteContext<T> extends BaseProcessingContext<T> {

	protected LrObjectId id;
	protected EntityParent<?> parent;

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
		this.id = new SimpleObjectId(id);
	}

	public void setCompoundId(Map<String, Object> ids) {
		this.id = new CompoundObjectId(ids);
	}

	public EntityParent<?> getParent() {
		return parent;
	}

	public void setParent(EntityParent<?> parent) {
		this.parent = parent;
	}
}
