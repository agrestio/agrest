package com.nhl.link.rest;

import java.util.Collection;
import java.util.Map;

import org.apache.cayenne.exp.Property;

/**
 * @since 1.4
 */
public interface DeleteBuilder<T> {

	DeleteBuilder<T> id(Object id);

	/**
	 * @param ids multi-attribute ID
	 * @since 1.20
	 */
	DeleteBuilder<T> id(Map<String, Object> ids);

	DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

	/**
	 * @since 1.20
	 */
	DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent);

	DeleteBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent);

	/**
	 * @since 1.20
	 */
	DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, Property<T> relationshipFromParent);

	DeleteBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent);

	/**
	 * @since 1.20
	 */
	DeleteBuilder<T> toManyParent(Class<?> parentType, Map<String, Object> parentIds,
			Property<? extends Collection<T>> relationshipFromParent);

	SimpleResponse delete();
}
