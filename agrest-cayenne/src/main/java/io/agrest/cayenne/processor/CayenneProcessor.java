package io.agrest.cayenne.processor;

import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;

/**
 * @since 3.7
 */
public class CayenneProcessor {

    private static final String CAYENNE_ROOT_ENTITY_KEY = CayenneRootResourceEntityExt.class.getName();
    private static final String CAYENNE_RELATED_ENTITY_KEY = CayenneRelatedResourceEntityExt.class.getName();

    /**
     * @since 5.0
     */
    public static CayenneResourceEntityExt getEntity(ResourceEntity<?> entity) {
        String key = (entity instanceof RootResourceEntity) ? CAYENNE_ROOT_ENTITY_KEY : CAYENNE_RELATED_ENTITY_KEY;
        return entity.getProperty(key);
    }

    /**
     * @since 5.0
     */
    public static <T> CayenneRootResourceEntityExt<T> getRootEntity(RootResourceEntity<T> entity) {
        return entity.getProperty(CAYENNE_ROOT_ENTITY_KEY);
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
        entity.setProperty(CAYENNE_ROOT_ENTITY_KEY, newExt);

        // copy MapBy owner's query to MapBy to ensure its own resolvers work properly
        if (entity.getMapBy() != null) {
            entity.getMapBy().setProperty(CAYENNE_ROOT_ENTITY_KEY, newExt);
        }

        return newExt;
    }

    /**
     * @since 5.0
     */
    public static <T> CayenneRelatedResourceEntityExt getRelatedEntity(RelatedResourceEntity<T> entity) {
        return entity.getProperty(CAYENNE_RELATED_ENTITY_KEY);
    }

    /**
     * @since 5.0
     */
    public static <T> CayenneRelatedResourceEntityExt getOrCreateRelatedEntity(RelatedResourceEntity<T> entity) {

        CayenneRelatedResourceEntityExt ext = getRelatedEntity(entity);
        if (ext != null) {
            return ext;
        }

        CayenneRelatedResourceEntityExt newExt = new CayenneRelatedResourceEntityExt();
        entity.setProperty(CAYENNE_RELATED_ENTITY_KEY, newExt);

        // copy MapBy owner's query to MapBy to ensure its own resolvers work properly
        if (entity.getMapBy() != null) {
            entity.getMapBy().setProperty(CAYENNE_RELATED_ENTITY_KEY, newExt);
        }

        return newExt;
    }
}
