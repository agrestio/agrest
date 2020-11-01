package io.agrest.base.protocol;

/**
 * Defines Agrest protocol query parameter names.
 *
 * @since 4.1
 */
public enum AgProtocol {

    /**
     * Expression used to filter a select result.
     */
    cayenneExp,

    /**
     * Sort direction. Can be one of {@link Dir#ASC}, {@link Dir#ASC_CI}, {@link Dir#DESC}, {@link Dir#DESC_CI}.
     * Must be used in conjunction with {@link #sort}.
     */
    dir,

    /**
     * Property path to exclude from response objects.
     */
    exclude,


    /**
     * Either a property path or a JSON object defining rules for including entity properties in a response
     */
    include,

    /**
     * Max objects to include in a result. Used to control result pagination.
     */
    limit,

    /**
     * Property path to use as a result map key. When present a result "data" is rendered as a map instead of a list.
     */
    mapBy,

    /**
     * Either a property path or a JSON object that defines result sorting. May be used in conjunction with {@link #dir}
     * parameter.
     */
    sort,

    /**
     * Defines how many objects to skip from the beginning of a result list. Used to control pagination.
     */
    start
}
