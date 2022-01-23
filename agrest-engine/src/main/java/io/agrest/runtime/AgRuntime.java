package io.agrest.runtime;

import io.agrest.AgRequestBuilder;
import io.agrest.DeleteBuilder;
import io.agrest.EntityDelete;
import io.agrest.MetadataBuilder;
import io.agrest.SelectBuilder;
import io.agrest.SimpleResponse;
import io.agrest.UnrelateBuilder;
import io.agrest.UpdateBuilder;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.meta.MetadataContext;
import io.agrest.runtime.processor.meta.MetadataProcessorFactory;
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

    @Deprecated
    private final MetadataProcessorFactory metadataProcessorFactory;

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
        this.metadataProcessorFactory = injector.getInstance(MetadataProcessorFactory.class);
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
        SelectContext<T> context = new SelectContext<>(type, requestBuilderFactory.builder(), injector);
        return toSelectBuilder(context);
    }

    private <T> SelectBuilder<T> toSelectBuilder(SelectContext<T> context) {
        return new DefaultSelectBuilder<>(context, selectProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> create(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, createProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, createOrUpdateProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, idempotentCreateOrUpdateProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, idempotentFullSyncProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UpdateBuilder<T> update(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, updateProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> UnrelateBuilder<T> unrelate(Class<T> type) {
        UnrelateContext<T> context = new UnrelateContext<>(type, injector);
        return new DefaultUnrelateBuilder<>(context, unrelateProcessorFactory);
    }

    /**
     * @since 5.0
     */
    public <T> DeleteBuilder<T> delete(Class<T> type) {
        DeleteContext<T> context = new DeleteContext<>(type, injector);
        return new DefaultDeleteBuilder<>(context, deleteProcessorFactory);
    }

    /**
     * @since 5.0
     * @deprecated since 5.0 as DELETE HTTP method should have no body. Can be replaced with "delete(Class).byId(id1).byId(id2)"
     */
    @Deprecated
    public <T> SimpleResponse delete(Class<T> root, Collection<EntityDelete<T>> deleted) {
        DeleteBuilder<T> builder = delete(root);
        deleted.forEach(entityDelete -> builder.id(entityDelete.getId()));
        return builder.sync();
    }

    /**
     * @since 5.0
     * @deprecated since 5.0, as Agrest now integrates with OpenAPI 3 / Swagger.
     */
    @Deprecated
    public <T> MetadataBuilder<T> metadata(Class<T> type) {
        MetadataContext<T> context = new MetadataContext<>(type, injector);
        return new DefaultMetadataBuilder<>(context, metadataProcessorFactory);
    }
}
