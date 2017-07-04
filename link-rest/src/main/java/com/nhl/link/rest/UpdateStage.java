package com.nhl.link.rest;

/**
 * Defines the names of standard update stages that can be used to insert custom processors into the update pipeline.
 * Stages ordinals are meaningful and correspond to the order of their execution by LinkRest.
 *
 * @since 2.7
 */
public enum UpdateStage {

    // Note that stages ordinals are meaningful. DO NOT REORDER CASUALLY!

    START,

    PARSE_REQUEST,

    APPLY_SERVER_PARAMS,

    UPDATE_DATA_STORE,

    FILL_RESPONSE
}
