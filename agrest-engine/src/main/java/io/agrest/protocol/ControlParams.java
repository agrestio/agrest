package io.agrest.protocol;

/**
 * Defines control parameters from the Agrest Protocol.
 *
 * @since 4.1
 */
public enum ControlParams {

    /**
     * @deprecated in the protocol v.1.1 (Agrest 4.1) in favor of {@link ControlParams#exp}. Will be supported indefinitely
     * for backwards compatibility.
     */
    @Deprecated
    cayenneExp("A deprecated alias for 'exp' parameter. Expression used to filter a select result"),

    /**
     * @deprecated in the protocol v.1.2 (Agrest 5.0) in favor of {@link ControlParams#direction}. Will be supported
     * indefinitely for backwards compatibility.
     */
    @Deprecated
    dir("A deprecated alis for the 'direction' parameter. Sort direction. Can be one of 'ASC','ASC_CI', 'DESC', 'DESC_CI'. Used in conjunction with 'sort'."),

    /**
     * @since 5.0, protocol v1.2
     */
    direction("Sort direction. Can be one of 'ASC','ASC_CI', 'DESC', 'DESC_CI'. Used in conjunction with 'sort'."),

    exclude("Property path to exclude from response objects."),

    /**
     * @since 4.1, protocol v1.1
     */
    exp("Expression used to filter a select result"),

    include("Either a property path or a JSON object defining rules for including entity properties in a response"),

    limit("Max objects to include in a result. Used to control result pagination."),

    mapBy("Property path to use as a result map key. When present a result 'data' is rendered as a map instead of a list."),

    sort("Either a property path or a JSON object that defines result sorting. May be used in conjunction with 'dir' parameter."),

    start("Defines how many objects to skip from the beginning of a result list. Used to control pagination.");

    public final String description;

    ControlParams(String description) {
        this.description = description;
    }
}
