package io.agrest;

/**
 * Defines the names of standard delete stages.  Stages ordinals are meaningful and correspond to the order of their
 * execution by Agrest.
 *
 * @since 2.7
 */
public enum DeleteStage {

    START,

    /**
     * A stage delete requests are converted to {@link io.agrest.runtime.processor.update.ChangeOperation} objects.
     *
     * @since 4.8
     */
    MAP_CHANGES,

    /**
     * A stage when delete authorization rules are applied to the
     * {@link io.agrest.runtime.processor.update.ChangeOperation} objects. If at least one rule is not satisfied,
     * delete chain is failed with "403 Forbidden" status.
     *
     * @since 4.8
     */
    AUTHORIZE_CHANGES,

    // TODO: split into delete and commit stages, like UpdateStage does
    DELETE_IN_DATA_STORE
}
