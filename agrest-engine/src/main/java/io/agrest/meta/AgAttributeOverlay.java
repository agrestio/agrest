package io.agrest.meta;

/**
 * @since 4.7
 */
public interface AgAttributeOverlay {

    String getName();

    /**
     * Resolves attribute overlay to an attribute.
     */
    AgAttribute resolve(AgAttribute maybeOverlaid);
}
