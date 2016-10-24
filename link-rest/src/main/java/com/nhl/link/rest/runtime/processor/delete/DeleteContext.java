package com.nhl.link.rest.runtime.processor.delete;

import com.nhl.link.rest.CompoundObjectId;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.SimpleObjectId;
import com.nhl.link.rest.processor.BaseProcessingContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @since 1.16
 */
public class DeleteContext<T> extends BaseProcessingContext<T> {

	protected Collection<LrObjectId> ids;
	protected EntityParent<?> parent;

	public DeleteContext(Class<T> type) {
		super(type);
	}

	public boolean isById() {
		return ids != null;
	}

	public Collection<LrObjectId> getIds() {
		return ids;
	}

	public void addId(Object id) {
		if (ids == null) {
			ids = new ArrayList<>();
		}
		ids.add(new SimpleObjectId(id));
	}

	public void addCompoundId(Map<String, Object> ids) {
		if (this.ids == null) {
			this.ids = new ArrayList<>();
		}
		this.ids.add(new CompoundObjectId(ids));
	}

	public EntityParent<?> getParent() {
		return parent;
	}

	public void setParent(EntityParent<?> parent) {
		this.parent = parent;
	}
}
