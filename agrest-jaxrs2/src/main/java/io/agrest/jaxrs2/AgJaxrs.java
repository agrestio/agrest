package io.agrest.jaxrs2;

import io.agrest.AgRequestBuilder;
import io.agrest.DeleteBuilder;
import io.agrest.SelectBuilder;
import io.agrest.UnrelateBuilder;
import io.agrest.UpdateBuilder;
import io.agrest.runtime.AgRuntime;

import javax.ws.rs.core.Configuration;

/**
 * Provides access to Agrest stack within the JAX-RS environment. Its static methods provide entry point to build a
 * JAX-RS-compatible Agrest runtime and use Agrest in the JAX-RS environment.
 *
 * @since 5.0
 * @deprecated in favor of Jakarta version (JAX-RS 3)
 */
@Deprecated(since = "5.0", forRemoval = true)
public class AgJaxrs {

    /**
     * @param config JAX-RS config object that holds Agrest runtime.
     * @return a newly created builder of {@link io.agrest.AgRequest}.
     */
    public static AgRequestBuilder request(Configuration config) {
        return runtime(config).request();
    }

    public static <T> SelectBuilder<T> select(Class<T> root, Configuration config) {
        return runtime(config).select(root);
    }

    public static <T> UpdateBuilder<T> create(Class<T> type, Configuration config) {
        return runtime(config).create(type);
    }

    public static <T> UpdateBuilder<T> createOrUpdate(Class<T> type, Configuration config) {
        return runtime(config).createOrUpdate(type);
    }

    public static <T> DeleteBuilder<T> delete(Class<T> root, Configuration config) {
        return runtime(config).delete(root);
    }

    public static <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type, Configuration config) {
        return runtime(config).idempotentCreateOrUpdate(type);
    }

    public static <T> UpdateBuilder<T> idempotentFullSync(Class<T> type, Configuration config) {
        return runtime(config).idempotentFullSync(type);
    }

    public static <T> UpdateBuilder<T> update(Class<T> type, Configuration config) {
        return runtime(config).update(type);
    }

    public static <T> UnrelateBuilder<T> unrelate(Class<T> type, Configuration config) {
        return runtime(config).unrelate(type);
    }

    /**
     * Returns Agrest runtime associated with the JAX-RS Configuration.
     */
    public static AgRuntime runtime(Configuration config) {
        return AgJaxrsFeature.getRuntime(config);
    }
}
