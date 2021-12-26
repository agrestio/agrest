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

    /**
     * @deprecated since 4.8 in favor of "getOrCreateExt().setSelect()"
     */
    @Deprecated
    public static <T> SelectQuery<T> getQuery(ResourceEntity<T> entity) {
        CayenneResourceEntityExt<T> ext = getCayenneEntity(entity);
        return ext != null ? ext.getSelect() : null;
    }

    /**
     * @deprecated since 4.8 in favor of "getOrCreateExt().setSelect()"
     */
    @Deprecated
    public static <T> void setQuery(ResourceEntity<T> entity, SelectQuery<T> query) {
        getOrCreateCayenneEntity(entity).setSelect(query);
    }
}
