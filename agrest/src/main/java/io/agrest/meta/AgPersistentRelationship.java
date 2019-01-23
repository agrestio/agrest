package io.agrest.meta;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgObjectId;

import java.util.Map;

/**
 * @since 1.12
 */
public interface AgPersistentRelationship extends AgRelationship {

	boolean isToDependentEntity();

	boolean isPrimaryKey();

	Map<String, Object> extractId(AgObjectId id); // TODO: move this to a separate place?

	Map<String, Object> extractId(JsonNode id); // TODO: move this to a separate place?
}
