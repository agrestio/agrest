package io.agrest;

/**
 * Defines the names of standard select stages that can be used to insert custom processors. Stages ordinals are
 * meaningful and correspond to the order of their execution by Agrest.
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

    FETCH_DATA,

    /**
     * A stage when the read filters are applied to the result object tree
     *
     * @since 4.8
     */
    FILTER_RESULT,

    /**
     * A stage when the result is encoded, or encoders are installed, depending on pipeline implementation
     *
     * @since 4.8
     */
    ENCODE
}
