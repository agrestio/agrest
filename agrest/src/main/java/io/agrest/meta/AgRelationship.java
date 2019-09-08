package io.agrest.meta;

import io.agrest.ResourceEntity;
import io.agrest.property.PropertyReader;

/**
 * @since 1.12
 */
public interface AgRelationship {

	String getName();

	/**
	 * @since 2.0
	 */
	AgEntity<?> getTargetEntity();

	boolean isToMany();

	/**
	 * @since 2.10
	 */
	PropertyReader getPropertyReader(ResourceEntity<?> entity);
}
