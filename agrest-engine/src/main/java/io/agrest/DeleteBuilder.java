package io.agrest;

import org.apache.cayenne.exp.Property;

import java.util.Collection;
import java.util.Map;

/**
 * @since 1.4
 */
public interface DeleteBuilder<T> {

    DeleteBuilder<T> id(Object id);

    /**
     * @param ids multi-attribute ID
     * @since 1.20
     */
    DeleteBuilder<T> id(Map<String, Object> ids);

    /**
     * @since 2.3
     */
    DeleteBuilder<T> id(AgObjectId id);

    DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

    /**
     * @since 1.20
     */
    DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent);

    /**
     * @deprecated since 3.6 as it uses Cayenne API in the method signature. Use {@link #parent(Class, Object, String)}
     */
    @Deprecated
    default DeleteBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
        return parent(parentType, parentId, relationshipFromParent.getName());
    }

    /**
     * @since 1.20
     * @deprecated since 3.6 as it uses Cayenne API in the method signature. Use {@link #parent(Class, Map, String)}
     */
    @Deprecated
    default DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, Property<T> relationshipFromParent) {
        return parent(parentType, parentIds, relationshipFromParent.getName());
    }

    /**
     * @deprecated since 3.6 as it uses Cayenne API in the method signature. Use {@link #parent(Class, Object, String)}
     */
    @Deprecated
    default DeleteBuilder<T> toManyParent(Class<?> parentType, Object parentId, Property<? extends Collection<T>> relationshipFromParent) {
        return parent(parentType, parentId, relationshipFromParent.getName());
    }

    /**
     * @since 1.20
     * @deprecated since 3.6 as it uses Cayenne API in the method signature. Use {@link #parent(Class, Object, String)}
     */
    @Deprecated
    default DeleteBuilder<T> toManyParent(Class<?> parentType, Map<String, Object> parentIds, Property<? extends Collection<T>> relationshipFromParent) {
        return parent(parentType, parentIds, relationshipFromParent.getName());
    }

    SimpleResponse delete();
}
