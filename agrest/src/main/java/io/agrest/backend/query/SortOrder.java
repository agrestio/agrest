package io.agrest.backend.query;

/**
 * Constants to order query results (the ORDER BY clause).
 *
 */
public enum SortOrder {
    /**
     * Ascending order, case sensitive
     */
    ASCENDING,

    /**
     * Ascending order, case insensitive
     */
    ASCENDING_INSENSITIVE,

    /**
     * Descending order, case sensitive
     */
    DESCENDING,

    /**
     * Descending order, case insensitive
     */
    DESCENDING_INSENSITIVE
}
