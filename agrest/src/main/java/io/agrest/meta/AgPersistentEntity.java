package io.agrest.meta;

import java.util.Collection;

/**
 * An entity model shared across Agrest stack.
 * 
 * @since 1.12
 */
public interface AgPersistentEntity<T, O> extends AgEntity<T> {

	O getObjEntity();

	AgPersistentAttribute getPersistentAttribute(String name);

	Collection<AgPersistentAttribute> getPersistentAttributes();
}
