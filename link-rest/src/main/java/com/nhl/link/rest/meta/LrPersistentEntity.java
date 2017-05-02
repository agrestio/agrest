package com.nhl.link.rest.meta;

import java.util.Collection;

/**
 * An entity model shared across LinkRest stack.
 * 
 * @since 1.12
 */
public interface LrPersistentEntity<T> extends LrEntity<T> {

	LrPersistentAttribute getPersistentAttribute(String name);

	Collection<LrPersistentAttribute> getPersistentAttributes();
}
