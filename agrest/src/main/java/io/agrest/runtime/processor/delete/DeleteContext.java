package io.agrest.runtime.processor.delete;

import io.agrest.CompoundObjectId;
import io.agrest.EntityParent;
import io.agrest.LrObjectId;
import io.agrest.SimpleObjectId;
import io.agrest.processor.BaseProcessingContext;

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

	public void addId(LrObjectId id) {
		if (this.ids == null) {
			this.ids = new ArrayList<>();
		}
		this.ids.add(id);
	}

	public EntityParent<?> getParent() {
		return parent;
	}

	public void setParent(EntityParent<?> parent) {
		this.parent = parent;
	}
}
