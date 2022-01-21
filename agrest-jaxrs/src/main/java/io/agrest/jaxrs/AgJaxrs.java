package io.agrest.jaxrs;

import io.agrest.AgRequestBuilder;
import io.agrest.DeleteBuilder;
import io.agrest.MetadataBuilder;
import io.agrest.SelectBuilder;
import io.agrest.UnrelateBuilder;
import io.agrest.UpdateBuilder;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.IAgService;
import io.agrest.runtime.request.IAgRequestBuilderFactory;

import javax.ws.rs.core.Configuration;

/**
 * A JAX-RS Feature that contains Agrest stack. Its static methods provide entry point to build a JAX-RS-compatible
 * Agrest runtime and use Agrest in the JAX-RS environment.
 *
 * @since 5.0
 */
public class AgJaxrs {

    /**
     * @param config JAX-RS config object that holds Agrest runtime.
     * @return a newly created builder of {@link io.agrest.AgRequest}.
     */
    public static AgRequestBuilder request(Configuration config) {
        return runtime(config).service(IAgRequestBuilderFactory.class).builder();
    }

    public static <T> SelectBuilder<T> select(Class<T> root, Configuration config) {
        return runtime(config).service(IAgService.class).select(root);
    }

    public static <T> UpdateBuilder<T> create(Class<T> type, Configuration config) {
        return runtime(config).service(IAgService.class).create(type);
    }

    public static <T> UpdateBuilder<T> createOrUpdate(Class<T> type, Configuration config) {
        return runtime(config).service(IAgService.class).createOrUpdate(type);
    }

    public static <T> DeleteBuilder<T> delete(Class<T> root, Configuration config) {
        return runtime(config).service(IAgService.class).delete(root);
    }

    public static <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type, Configuration config) {
        return runtime(config).service(IAgService.class).idempotentCreateOrUpdate(type);
    }

    public static <T> UpdateBuilder<T> idempotentFullSync(Class<T> type, Configuration config) {
        return runtime(config).service(IAgService.class).idempotentFullSync(type);
    }

    public static <T> UpdateBuilder<T> update(Class<T> type, Configuration config) {
        return runtime(config).service(IAgService.class).update(type);
    }

    public static <T> UnrelateBuilder<T> unrelate(Class<T> type, Configuration config) {
        return runtime(config).service(IAgService.class).unrelate(type);
    }

    /**
     * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
     */
    @Deprecated
    public static <T> MetadataBuilder<T> metadata(Class<T> entityClass, Configuration config) {
        return runtime(config).service(IAgService.class).metadata(entityClass);
    }

    /**
     * Returns Agrest runtime associated with the JAX-RS Configuration.
     */
    public static AgRuntime runtime(Configuration config) {
        return AgJaxrsFeature.getRuntime(config);
    }
}
