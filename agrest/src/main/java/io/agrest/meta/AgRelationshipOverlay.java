package io.agrest.meta;

/**
 * @since 3.4
 */
public interface AgRelationshipOverlay {

    String getName();

    /**
     * Resolves relationship overlay to a relationship.
     *
     * @return resolved relationship or null if it can't be resolved.
     */
    AgRelationship resolve(AgRelationship maybeOverlaid, AgDataMap agDataMap);
}
