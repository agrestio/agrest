package io.agrest;

/**
 * Defines the names of standard update stages that can be used to insert custom processors into the update pipeline.
 * Stages ordinals are meaningful and correspond to the order of their execution by Agrest.
 *
 * @since 2.7
 */
public enum UpdateStage {

    // Stages ordinals determine stage execution order. DO NOT REORDER WITHOUT A REASON!

    START,

    PARSE_REQUEST,

    CREATE_ENTITY,

    APPLY_SERVER_PARAMS,

    /**
     * A stage when client changes are converted to {@link io.agrest.runtime.processor.update.ChangeOperation} objects.
     *
     * @since 4.8
     */
    MAP_CHANGES,

    /**
     * A stage when authorization rules are applied to the {@link io.agrest.runtime.processor.update.ChangeOperation}
     * objects. If at least one rule is not satisfied, the update chain is failed with "403 Forbidden" status.
     *
     * @since 4.8
     */
    AUTHORIZE_CHANGES,

    MERGE_CHANGES,

    COMMIT,

    FILL_RESPONSE
}
