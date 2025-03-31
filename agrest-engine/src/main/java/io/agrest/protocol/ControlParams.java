package io.agrest.protocol;

/**
 * Defines control parameters from the Agrest Protocol.
 *
 * @since 4.1
 */
public enum ControlParams {

    /**
     * A deprecated alias for 'exp' parameter. Expression used to filter a select result
     *
     * @deprecated in the protocol v.1.1 (Agrest 4.1) in favor of {@link ControlParams#exp}. Will be supported indefinitely
     * for backwards compatibility.
     */
    @Deprecated(forRemoval = false)
    cayenneExp("A deprecated alias for 'exp' parameter. Expression used to filter a select result"),

    /**
     * A deprecated alias for the 'direction' parameter. Sort direction. Can be one of 'ASC','ASC_CI', 'DESC', 'DESC_CI'.
     * Used in conjunction with 'sort'.
     *
     * @deprecated in the protocol v.1.2 (Agrest 5.0) in favor of {@link ControlParams#direction}. Will be supported
     * indefinitely for backwards compatibility.
     */
    @Deprecated(forRemoval = false)
    dir("A deprecated alias for the 'direction' parameter. Sort direction. Can be one of 'ASC','ASC_CI', 'DESC', 'DESC_CI'. Used in conjunction with 'sort'."),

    /**
     * Sort direction. Can be one of 'ASC','ASC_CI', 'DESC', 'DESC_CI'. Used in conjunction with 'sort'.
     *
     * @since 5.0, protocol v1.2
     */
    direction("Sort direction. Can be one of 'ASC','ASC_CI', 'DESC', 'DESC_CI'. Used in conjunction with 'sort'."),

    /**
     * Property path to exclude from response objects
     */
    exclude("Property path to exclude from response objects."),

    /**
     * Expression used to filter a select result
     *
     * @since 4.1, protocol v1.1
     */
    exp("Expression used to filter a select result"),

    /**
     * Either a property path or a JSON object defining rules for including entity properties in a response
     */
    include("Either a property path or a JSON object defining rules for including entity properties in a response"),

    /**
     * Max objects to include in a result. Used to control result pagination.
     */
    limit("Max objects to include in a result. Used to control result pagination."),

    /**
     * Property path to use as a result map key. When present a result 'data' is rendered as a map instead of a list.
     */
    mapBy("Property path to use as a result map key. When present a result 'data' is rendered as a map instead of a list."),

    /**
     * Either a property path or a JSON object that defines result sorting. May be used in conjunction with 'dir' parameter.
     */
    sort("Either a property path or a JSON object that defines result sorting. May be used in conjunction with 'dir' parameter."),

    /**
     * Defines how many objects to skip from the beginning of a result list. Used to control pagination.
     */
    start("Defines how many objects to skip from the beginning of a result list. Used to control pagination.");


    // define String constants for parameter names to be used in annotations. Using String constants instead of
    // ControlParams.field.name() as otherwise these definitions won't be usable in annotations

    /**
     * @since 5.0
     * @deprecated in the protocol v.1.1 (Agrest 4.1) in favor of {@link #EXP}. Will be supported indefinitely
     * for backwards compatibility.
     */
    @Deprecated(forRemoval = false)
    public static final String CAYENNE_EXP = "cayenneExp";

    /**
     * @since 5.0
     * @deprecated in the protocol v.1.2 (Agrest 5.0) in favor of {@link #direction}. Will be supported
     * indefinitely for backwards compatibility.
     */
    @Deprecated(forRemoval = false)
    public static final String DIR = "dir";

    /**
     * Sort direction. Can be one of 'ASC','ASC_CI', 'DESC', 'DESC_CI'. Used in conjunction with 'sort'.
     *
     * @see #direction
     * @since 5.0
     */
    public static final String DIRECTION = "direction";

    /**
     * @since 5.0
     */
    public static final String EXCLUDE = "exclude";

    /**
     * @since 5.0
     */
    public static final String EXP = "exp";

    /**
     * @since 5.0
     */
    public static final String INCLUDE = "include";

    /**
     * @since 5.0
     */
    public static final String LIMIT = "limit";

    /**
     * @since 5.0
     */
    public static final String MAP_BY = "mapBy";

    /**
     * @since 5.0
     */
    public static final String SORT = "sort";

    /**
     * @since 5.0
     */
    public static final String START = "start";

    public final String description;

    ControlParams(String description) {
        this.description = description;
    }
}
