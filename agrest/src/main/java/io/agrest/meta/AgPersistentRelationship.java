package io.agrest.meta;

import io.agrest.AgObjectId;

import java.util.Map;

/**
 * @since 1.12
 */
public interface AgPersistentRelationship extends AgRelationship {

	boolean isToDependentEntity();

	Map<String, Object> extractId(AgObjectId id); // TODO: move this to a separate place?
}
