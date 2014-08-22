package com.nhl.link.rest.runtime;

import java.util.Collection;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.SimpleResponse;

/**
 * @since 1.4
 */
public abstract class BaseDeleteBuilder<T> implements DeleteBuilder<T> {

	protected Class<T> type;
	protected Object id;
	protected EntityParent<?> parent;

	public BaseDeleteBuilder(Class<T> type) {
		this.type = type;
	}

	@Override
	public DeleteBuilder<T> id(Object id) {
		this.id = id;
		return this;
	}

	@Override
	public DeleteBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
		this.parent = new EntityParent<>(parentType, parentId, relationshipFromParent.getName());
		return this;
	}

	@Override
	public DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		this.parent = new EntityParent<>(parentType, parentId, relationshipFromParent);
		return this;
	}

	@Override
	public DeleteBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentId, relationshipFromParent.getName());
	}

	@Override
	public abstract SimpleResponse delete();
}
