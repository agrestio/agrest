package io.agrest.cayenne.processor;

import io.agrest.ResourceEntity;
import org.apache.cayenne.query.SelectQuery;

/**
 * @since 3.7
 */
public class CayenneProcessor {

    private static final String SELECT_QUERY_KEY = "io.agrest.cayenne.processor.SelectQuery";

    // TODO: the actual query is a column query for nested entities and an object query for root
    public static <T> SelectQuery<T> getQuery(ResourceEntity<?> entity) {
        return entity.getRequestProperty(SELECT_QUERY_KEY);
    }

    public static <T> void setQuery(ResourceEntity<T> entity, SelectQuery<T> query) {
        entity.setRequestProperty(SELECT_QUERY_KEY, query);

        // copy MapBy owner's query to MapBy to ensure its own resolvers work properly
        if (entity.getMapBy() != null) {
            entity.getMapBy().setRequestProperty(SELECT_QUERY_KEY, query);
        }
    }
}
