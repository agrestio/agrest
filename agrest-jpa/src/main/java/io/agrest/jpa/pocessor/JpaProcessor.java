package io.agrest.jpa.pocessor;

import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;

/**
 * @since 5.0
 */
public class JpaProcessor {

    private static final String JPA_ROOT_ENTITY_KEY = JpaRootResourceEntityExt.class.getName();
    private static final String JPA_NESTED_ENTITY_KEY = JpaNestedResourceEntityExt.class.getName();

    public static JpaResourceEntityExt getEntity(ResourceEntity<?> entity) {
        String key = (entity instanceof RootResourceEntity) ? JPA_ROOT_ENTITY_KEY : JPA_NESTED_ENTITY_KEY;
        return entity.getRequestProperty(key);
    }

    public static <T> JpaRootResourceEntityExt<T> getRootEntity(RootResourceEntity<T> entity) {
        return entity.getRequestProperty(JPA_ROOT_ENTITY_KEY);
    }

    public static <T> JpaRootResourceEntityExt<T> getOrCreateRootEntity(RootResourceEntity<T> entity) {
        JpaRootResourceEntityExt<T> ext = getRootEntity(entity);
        if (ext != null) {
            return ext;
        }

        JpaRootResourceEntityExt<T> newExt = new JpaRootResourceEntityExt<>();
        entity.setRequestProperty(JPA_ROOT_ENTITY_KEY, newExt);

        // copy MapBy owner's query to MapBy to ensure its own resolvers work properly
        if (entity.getMapBy() != null) {
            entity.getMapBy().setRequestProperty(JPA_ROOT_ENTITY_KEY, newExt);
        }

        return newExt;
    }

    public static <T> JpaNestedResourceEntityExt getNestedEntity(RelatedResourceEntity<T> entity) {
        return entity.getRequestProperty(JPA_NESTED_ENTITY_KEY);
    }

    public static <T> JpaNestedResourceEntityExt getOrCreateNestedEntity(RelatedResourceEntity<T> entity) {

        JpaNestedResourceEntityExt ext = getNestedEntity(entity);
        if (ext != null) {
            return ext;
        }

        JpaNestedResourceEntityExt newExt = new JpaNestedResourceEntityExt();
        entity.setRequestProperty(JPA_NESTED_ENTITY_KEY, newExt);

        // copy MapBy owner's query to MapBy to ensure its own resolvers work properly
        if (entity.getMapBy() != null) {
            entity.getMapBy().setRequestProperty(JPA_NESTED_ENTITY_KEY, newExt);
        }

        return newExt;
    }
}
