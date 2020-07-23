package io.agrest;

/**
 * Defines the names of standard update stages that can be used to insert custom processors into the update pipeline.
 * Stages ordinals are meaningful and correspond to the order of their execution by Agrest.
 *
 * @since 2.7
 */
public enum UpdateStage {

    // Stages ordinals are determine stage execution order. DO NOT REORDER WITHOUT A REASON!

    START,

    PARSE_REQUEST,

    CREATE_ENTITY,

    APPLY_SERVER_PARAMS,

    MERGE_CHANGES,

    COMMIT,

    FILL_RESPONSE
}
