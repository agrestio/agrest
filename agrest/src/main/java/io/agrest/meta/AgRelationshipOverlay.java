package io.agrest.meta;

/**
 * @since 3.4
 */
public interface AgRelationshipOverlay {

    String getName();

    AgRelationship resolve(AgRelationship maybeOverlaid, AgDataMap agDataMap);
}
