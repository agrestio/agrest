package io.agrest.meta;

import java.util.Collection;

import org.apache.cayenne.map.ObjEntity;

/**
 * An entity model shared across LinkRest stack.
 * 
 * @since 1.12
 */
public interface LrPersistentEntity<T> extends LrEntity<T> {

	ObjEntity getObjEntity();

	LrPersistentAttribute getPersistentAttribute(String name);

	Collection<LrPersistentAttribute> getPersistentAttributes();
}
