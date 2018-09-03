package io.agrest;

/**
 * Defines the names of standard select stages that can be used to insert custom processors. Stages ordinals are
 * meaningful and correspond to the order of their execution by LinkRest.
 *
 * @since 2.7
 */
public enum SelectStage {

    // Note that stages ordinals are meaningful. DO NOT REORDER CASUALLY!

    START,

    PARSE_REQUEST,

    CREATE_ENTITY,

    APPLY_SERVER_PARAMS,

    ASSEMBLE_QUERY,

    FETCH_DATA
}
