package com.nhl.link.rest;

import java.util.Collection;

import org.apache.cayenne.exp.Property;

/**
 * @since 1.4
 */
public interface DeleteBuilder<T> {

	DeleteBuilder<T> id(Object id);

	DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

	DeleteBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent);

	DeleteBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent);

	SimpleResponse delete();
}
