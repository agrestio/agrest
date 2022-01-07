package io.agrest;

/**
 * @since 5.0
 */
public interface UnrelateBuilder<T> {

    UnrelateBuilder<T> sourceId(Object id);

    UnrelateBuilder<T> allRelated(String relationship);

    UnrelateBuilder<T> related(String relationship, Object targetId);

    /**
     * Executes "unrelate" pipeline for the request configured in this builder.
     */
    SimpleResponse sync();
}
