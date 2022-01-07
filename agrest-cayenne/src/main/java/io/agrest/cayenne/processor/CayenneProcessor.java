package io.agrest.cayenne.processor;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;

/**
 * @since 3.7
 */
public class CayenneProcessor {

    private static final String CAYENNE_ROOT_ENTITY_KEY = CayenneRootResourceEntityExt.class.getName();
    private static final String CAYENNE_NESTED_ENTITY_KEY = CayenneNestedResourceEntityExt.class.getName();

    /**
     * @since 5.0
     */
    public static CayenneResourceEntityExt getEntity(ResourceEntity<?> entity) {
        String key = (entity instanceof RootResourceEntity) ? CAYENNE_ROOT_ENTITY_KEY : CAYENNE_NESTED_ENTITY_KEY;
        return entity.getRequestProperty(key);
    }

    /**
     * @since 5.0
     */
    public static <T> CayenneRootResourceEntityExt<T> getRootEntity(RootResourceEntity<T> entity) {
        return entity.getRequestProperty(CAYENNE_ROOT_ENTITY_KEY);
    }

    /**
     * @since 5.0
     */
    public static <T> CayenneRootResourceEntityExt<T> getOrCreateRootEntity(RootResourceEntity<T> entity) {

        CayenneRootResourceEntityExt<T> ext = getRootEntity(entity);
        if (ext != null) {
            return ext;
        }

        CayenneRootResourceEntityExt<T> newExt = new CayenneRootResourceEntityExt<>();
        entity.setRequestProperty(CAYENNE_ROOT_ENTITY_KEY, newExt);

        // copy MapBy owner's query to MapBy to ensure its own resolvers work properly
        if (entity.getMapBy() != null) {
            entity.getMapBy().setRequestProperty(CAYENNE_ROOT_ENTITY_KEY, newExt);
        }

        return newExt;
    }

    /**
     * @since 5.0
     */
    public static <T> CayenneNestedResourceEntityExt getNestedEntity(NestedResourceEntity<T> entity) {
        return entity.getRequestProperty(CAYENNE_NESTED_ENTITY_KEY);
    }

    /**
     * @since 5.0
     */
    public static <T> CayenneNestedResourceEntityExt getOrCreateNestedEntity(NestedResourceEntity<T> entity) {

        CayenneNestedResourceEntityExt ext = getNestedEntity(entity);
        if (ext != null) {
            return ext;
        }

        CayenneNestedResourceEntityExt newExt = new CayenneNestedResourceEntityExt();
        entity.setRequestProperty(CAYENNE_NESTED_ENTITY_KEY, newExt);

        // copy MapBy owner's query to MapBy to ensure its own resolvers work properly
        if (entity.getMapBy() != null) {
            entity.getMapBy().setRequestProperty(CAYENNE_NESTED_ENTITY_KEY, newExt);
        }

        return newExt;
    }
}
