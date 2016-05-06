package com.nhl.link.rest.runtime.processor.unrelate;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.processor.BaseProcessingContext;

/**
 * @since 1.16
 */
public class UnrelateContext<T> extends BaseProcessingContext<T> {

	private EntityParent<?> parent;
	private Object id;

	public UnrelateContext(Class<T> type) {
		super(type);
	}

	public EntityParent<?> getParent() {
		return parent;
	}

	public void setParent(EntityParent<?> parent) {
		this.parent = parent;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object targetId) {
		this.id = targetId;
	}
}
