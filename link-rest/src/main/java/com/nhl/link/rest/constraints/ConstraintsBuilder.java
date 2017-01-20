package com.nhl.link.rest.constraints;

/**
 * Defines read or write constraints on a given entity. Constraints are
 * predefined on the server side and are applied to each request, ensuring a
 * client can't read or write more data than she is allowed to.
 *
 * @since 1.3
 * @deprecated since 2.4 in favor of {@link Constraints}.
 */
@Deprecated
public class ConstraintsBuilder<T> {

    /**
     * @param type a root type for constraints.
     * @param <T>  LinkRest entity type.
     * @return a new Constraints instance.
     * @since 1.5
     * @deprecated currently an alias to {@link Constraints#excludeAll(Class)}.
     */
    public static <T> Constraints<T> excludeAll(Class<T> type) {
        return Constraints.excludeAll(type);
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  LinkRest entity type.
     * @return a new Constraints instance.
     * @since 1.5
     * @deprecated currently an alias to {@link Constraints#idOnly(Class)}.
     */
    public static <T> Constraints<T> idOnly(Class<T> type) {
        return Constraints.idOnly(type);
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  LinkRest entity type.
     * @return a new Constraints instance.
     * @since 1.5
     * @deprecated currently an alias to {@link Constraints#idAndAttributes(Class)}.
     */
    public static <T> Constraints<T> idAndAttributes(Class<T> type) {
        return Constraints.idAndAttributes(type);
    }

    private ConstraintsBuilder() {
    }
}
