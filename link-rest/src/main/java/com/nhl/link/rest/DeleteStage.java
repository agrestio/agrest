package com.nhl.link.rest;

/**
 * Defines the names of standard delete stages.  Stages ordinals are meaningful and correspond to the order of their
 * execution by LinkRest.
 *
 * @since 2.7
 */
public enum DeleteStage {

    START,

    DELETE_IN_DATA_STORE
}
