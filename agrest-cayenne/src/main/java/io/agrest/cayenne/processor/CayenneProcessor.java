package io.agrest.cayenne.processor;

import io.agrest.ResourceEntity;
import org.apache.cayenne.query.SelectQuery;

/**
 * @since 3.7
 */
public class CayenneProcessor {

    private static final String CAYENNE_EXT_KEY = CayenneResourceEntityExt.class.getName();

    /**
     * @since 4.8
     */
    public static <T> CayenneResourceEntityExt<T> getCayenneEntity(ResourceEntity<T> entity) {
        return entity.getRequestProperty(CAYENNE_EXT_KEY);
    }

    /**
     * @since 4.8
     */
    public static <T> CayenneResourceEntityExt getOrCreateCayenneEntity(ResourceEntity<T> entity) {

        CayenneResourceEntityExt<T> ext = getCayenneEntity(entity);
        if (ext != null) {
            return ext;
        }

        CayenneResourceEntityExt<T> newExt = new CayenneResourceEntityExt<>();
        entity.setRequestProperty(CAYENNE_EXT_KEY, newExt);

        // copy MapBy owner's query to MapBy to ensure its own resolvers work properly
        if (entity.getMapBy() != null) {
            entity.getMapBy().setRequestProperty(CAYENNE_EXT_KEY, newExt);
        }

        return newExt;
    }
}
