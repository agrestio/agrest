package io.agrest.meta;

/**
 * @since 3.4
 */
public interface AgRelationshipOverlay {

    String getName();

    /**
     * Resolves relationship overlay to a relationship.
     */
    AgRelationship resolve(AgRelationship maybeOverlaid, AgDataMap agDataMap);
}
