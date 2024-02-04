package io.agrest.runtime;

import io.agrest.AgRequestBuilder;
import io.agrest.DeleteBuilder;
import io.agrest.EntityDelete;
import io.agrest.SelectBuilder;
import io.agrest.SimpleResponse;
import io.agrest.UnrelateBuilder;
import io.agrest.UpdateBuilder;
import io.agrest.access.PathChecker;
import io.agrest.meta.AgSchema;
import io.agrest.runtime.meta.RequestSchema;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.CreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.CreateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentCreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentFullSyncProcessorFactory;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateProcessorFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Represents Agrest stack.
 */
public class AgRuntime {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgRuntime.class);

    private final Injector injector;

    private final IAgRequestBuilderFactory requestBuilderFactory;
    private final SelectProcessorFactory selectProcessorFactory;
    private final DeleteProcessorFactory deleteProcessorFactory;
    private final CreateProcessorFactory createProcessorFactory;
    private final UpdateProcessorFactory updateProcessorFactory;
    private final CreateOrUpdateProcessorFactory createOrUpdateProcessorFactory;
    private final IdempotentCreateOrUpdateProcessorFactory idempotentCreateOrUpdateProcessorFactory;
    private final IdempotentFullSyncProcessorFactory idempotentFullSyncProcessorFactory;
    private final UnrelateProcessorFactory unrelateProcessorFactory;

    private final AgSchema schema;
    private final PathChecker pathChecker;

    /**
     * Creates and returns a default Agrest runtime
     *
     * @see #builder()
     * @since 5.0
     */
    public static AgRuntime build() {
        return new AgRuntimeBuilder().build();
    }

    /**
     * Creates and returns a builder of Agrest runtime.
     *
     * @since 5.0
     */
    public static AgRuntimeBuilder builder() {
        return new AgRuntimeBuilder();
    }

    protected AgRuntime(Injector injector) {
        this.injector = injector;

        // cache commonly-used services
        this.requestBuilderFactory = injector.getInstance(IAgRequestBuilderFactory.class);
        this.selectProcessorFactory = injector.getInstance(SelectProcessorFactory.class);
        this.deleteProcessorFactory = injector.getInstance(DeleteProcessorFactory.class);
        this.createProcessorFactory = injector.getInstance(CreateProcessorFactory.class);
        this.updateProcessorFactory = injector.getInstance(UpdateProcessorFactory.class);
        this.createOrUpdateProcessorFactory = injector.getInstance(CreateOrUpdateProcessorFactory.class);
        this.idempotentCreateOrUpdateProcessorFactory = injector.getInstance(IdempotentCreateOrUpdateProcessorFactory.class);
        this.idempotentFullSyncProcessorFactory = injector.getInstance(IdempotentFullSyncProcessorFactory.class);
        this.unrelateProcessorFactory = injector.getInstance(UnrelateProcessorFactory.class);

        this.schema = injector.getInstance(AgSchema.class);
        this.pathChecker = injector.getInstance(PathChecker.class);
    }

    /**
     * Returns an instance of an internal service of a given type.
     */
    public <T> T service(Class<T> type) {
        return injector.getInstance(type);
    }

    /**
     * Returns an instance of an internal service matching a given DI key.
     *
     * @since 2.10
     */
    public <T> T service(Key<T> key) {
        return injector.getInstance(key);
    }

    /**
     * @since 2.0
     */
    public void shutdown() {
        LOGGER.info("Shutting down Agrest");
        injector.shutdown();
    }

    /**
     * @since 5.0
     */
    public AgRequestBuilder request() {
        return requestBuilderFactory.builder();
    }

    /**
     * @since 5.0
     */
    public <T> SelectBuilder<T> select(Class<T> type) {
        return new DefaultSelectBuilder<>(createSelectContext(type), selectProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> create(Class<T> type) {
        return new DefaultUpdateBuilder<>(createUpdateContext(type), createProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
        return new DefaultUpdateBuilder<>(createUpdateContext(type), createOrUpdateProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
        return new DefaultUpdateBuilder<>(createUpdateContext(type), idempotentCreateOrUpdateProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
        return new DefaultUpdateBuilder<>(createUpdateContext(type), idempotentFullSyncProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> update(Class<T> type) {
        return new DefaultUpdateBuilder<>(createUpdateContext(type), updateProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UnrelateBuilder<T> unrelate(Class<T> type) {
        return new DefaultUnrelateBuilder<>(createUnrelateContext(type), unrelateProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> DeleteBuilder<T> delete(Class<T> type) {
        return new DefaultDeleteBuilder<>(createDeleteContext(type), deleteProcessorFactory);
    }

    /**
     * @since 5.0
     * @deprecated since 5.0 as DELETE HTTP method should have no body. Can be replaced with "delete(Class).byId(id1).byId(id2)"
     */
    @Deprecated
    public <T> SimpleResponse delete(Class<T> root, Collection<EntityDelete<T>> deleted) {
        DeleteBuilder<T> builder = delete(root);
        deleted.forEach(entityDelete -> builder.byId(entityDelete.getId()));
        return builder.sync();
    }

    private <T> SelectContext<T> createSelectContext(Class<T> type) {
        return new SelectContext<>(
                type,
                new RequestSchema(schema),
                request(),
                pathChecker,
                injector);
    }

    private <T> UpdateContext<T> createUpdateContext(Class<T> type) {
        return new UpdateContext<>(
                type,
                new RequestSchema(schema),
                request(),
                pathChecker,
                injector);
    }

    private <T> UnrelateContext<T> createUnrelateContext(Class<T> type) {
        return new UnrelateContext<>(type, schema, injector);
    }

    private <T> DeleteContext<T> createDeleteContext(Class<T> type) {
        return new DeleteContext<>(type, new RequestSchema(schema), injector);
    }
}
