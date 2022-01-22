package io.agrest.runtime;

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
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Injector;

import java.util.Collection;

/**
 * A backend-agnostic abstract {@link IAgService} that can serve to
 * implement more specific versions.
 */
public class DefaultAgService implements IAgService {

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

    public DefaultAgService(
            @Inject Injector injector,
            @Inject IAgRequestBuilderFactory requestBuilderFactory,
            @Inject SelectProcessorFactory selectProcessorFactory,
            @Inject DeleteProcessorFactory deleteProcessorFactory,
            @Inject CreateProcessorFactory createProcessorFactory,
            @Inject UpdateProcessorFactory updateProcessorFactory,
            @Inject CreateOrUpdateProcessorFactory createOrUpdateProcessorFactory,
            @Inject IdempotentCreateOrUpdateProcessorFactory idempotentCreateOrUpdateProcessorFactory,
            @Inject IdempotentFullSyncProcessorFactory idempotentFullSyncProcessorFactory,
            @Inject UnrelateProcessorFactory unrelateProcessorFactory,
            @Inject MetadataProcessorFactory metadataProcessorFactory) {

        this.injector = injector;

        this.requestBuilderFactory = requestBuilderFactory;

        this.selectProcessorFactory = selectProcessorFactory;
        this.deleteProcessorFactory = deleteProcessorFactory;
        this.createProcessorFactory = createProcessorFactory;
        this.updateProcessorFactory = updateProcessorFactory;
        this.createOrUpdateProcessorFactory = createOrUpdateProcessorFactory;
        this.idempotentCreateOrUpdateProcessorFactory = idempotentCreateOrUpdateProcessorFactory;
        this.idempotentFullSyncProcessorFactory = idempotentFullSyncProcessorFactory;
        this.unrelateProcessorFactory = unrelateProcessorFactory;

        this.metadataProcessorFactory = metadataProcessorFactory;
    }

    @Override
    public <T> SelectBuilder<T> select(Class<T> type) {
        SelectContext<T> context = new SelectContext<>(type, requestBuilderFactory.builder(), injector);
        return toSelectBuilder(context);
    }

    private <T> SelectBuilder<T> toSelectBuilder(SelectContext<T> context) {
        return new DefaultSelectBuilder<>(context, selectProcessorFactory);
    }

    @Deprecated
    @Override
    public <T> SimpleResponse delete(Class<T> root, Collection<EntityDelete<T>> deleted) {
        DeleteBuilder<T> builder = delete(root);
        deleted.forEach(entityDelete -> builder.id(entityDelete.getId()));
        return builder.sync();
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> create(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, createProcessorFactory);
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, createOrUpdateProcessorFactory);
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, idempotentCreateOrUpdateProcessorFactory);
    }

    /**
     * @since 1.7
     */
    @Override
    public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, idempotentFullSyncProcessorFactory);
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> update(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, requestBuilderFactory.builder(), injector);
        return new DefaultUpdateBuilder<>(context, updateProcessorFactory);
    }

    /**
     * @since 5.0
     */
    @Override
    public <T> UnrelateBuilder<T> unrelate(Class<T> type) {
        UnrelateContext<T> context = new UnrelateContext<>(type, injector);
        return new DefaultUnrelateBuilder<>(context, unrelateProcessorFactory);
    }

    /**
     * @since 1.4
     */
    @Override
    public <T> DeleteBuilder<T> delete(Class<T> type) {
        DeleteContext<T> context = new DeleteContext<>(type, injector);
        return new DefaultDeleteBuilder<>(context, deleteProcessorFactory);
    }

    @Override
    public <T> MetadataBuilder<T> metadata(Class<T> type) {
        MetadataContext<T> context = new MetadataContext<>(type, injector);
        return new DefaultMetadataBuilder<>(context, metadataProcessorFactory);
    }
}
