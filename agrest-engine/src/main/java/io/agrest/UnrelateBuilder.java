package io.agrest;

import java.util.Map;

/**
 * @since 5.0
 */
public interface UnrelateBuilder<T> {

    UnrelateBuilder<T> sourceId(Object id);

    /**
     * @since 5.0
     */
    UnrelateBuilder<T> sourceId(Map<String, Object> ids);

    UnrelateBuilder<T> allRelated(String relationship);

    UnrelateBuilder<T> related(String relationship, Object targetId);

    /**
     * @since 5.0
     */
    UnrelateBuilder<T> related(String relationship, Map<String, Object> targetId);

    /**
     * Executes "unrelate" pipeline for the request configured in this builder.
     */
    SimpleResponse sync();
}
