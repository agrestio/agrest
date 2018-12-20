package io.agrest;

import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.IAgService;

import javax.ws.rs.core.Configuration;

/**
 * Defines static methods to start Agrest request processor builders. Users of this class must inject
 * {@link Configuration} instance to pass to the static methods.
 *
 * @since 1.14
 */
public class Ag {

    public static <T, E> SelectBuilder<T, E> select(Class<T> root, Configuration config) {
        return service(config).select(root);
    }

    public static <T, E> UpdateBuilder<T, E> create(Class<T> type, Configuration config) {
        return service(config).create(type);
    }

    public static <T, E> UpdateBuilder<T, E> createOrUpdate(Class<T> type, Configuration config) {
        return service(config).createOrUpdate(type);
    }

    public static <T> DeleteBuilder<T> delete(Class<T> root, Configuration config) {
        return service(config).delete(root);
    }

    public static <T, E> UpdateBuilder<T, E> idempotentCreateOrUpdate(Class<T> type, Configuration config) {
        return service(config).idempotentCreateOrUpdate(type);
    }

    public static <T, E> UpdateBuilder<T, E> idempotentFullSync(Class<T> type, Configuration config) {
        return service(config).idempotentFullSync(type);
    }

    public static <T, E> UpdateBuilder<T, E> update(Class<T> type, Configuration config) {
        return service(config).update(type);
    }

    /**
     * @since 1.18
     */
    public static <T> MetadataBuilder<T> metadata(Class<T> entityClass, Configuration config) {
        return service(config).metadata(entityClass);
    }

    /**
     * Returns {@link IAgService} bound to a given JAX RS configuration.
     * IAgService is the main engine behind all the operations in
     * Agrest, however you would rarely need to use it directly. Instead use
     * other static methods defined in this class to start processor chains for
     * Agrest requests.
     */
    public static IAgService service(Configuration config) {
        return AgRuntime.service(IAgService.class, config);
    }
}
