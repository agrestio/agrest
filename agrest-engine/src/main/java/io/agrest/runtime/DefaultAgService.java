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
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateProcessorFactoryFactory;
import org.apache.cayenne.di.Inject;

import java.util.Collection;

/**
 * A backend-agnostic abstract {@link IAgService} that can serve to
 * implement more specific versions.
 */
public class DefaultAgService implements IAgService {

    private final SelectProcessorFactory selectProcessorFactory;
    private final DeleteProcessorFactory deleteProcessorFactory;
    private final UpdateProcessorFactoryFactory updateProcessorFactoryFactory;
    private final MetadataProcessorFactory metadataProcessorFactory;
    private final UnrelateProcessorFactory unrelateProcessorFactory;

    public DefaultAgService(
            @Inject SelectProcessorFactory selectProcessorFactory,
            @Inject DeleteProcessorFactory deleteProcessorFactory,
            @Inject UpdateProcessorFactoryFactory updateProcessorFactoryFactory,
            @Inject MetadataProcessorFactory metadataProcessorFactory,
            @Inject UnrelateProcessorFactory unrelateProcessorFactory) {

        this.selectProcessorFactory = selectProcessorFactory;
        this.deleteProcessorFactory = deleteProcessorFactory;
        this.updateProcessorFactoryFactory = updateProcessorFactoryFactory;
        this.metadataProcessorFactory = metadataProcessorFactory;
        this.unrelateProcessorFactory = unrelateProcessorFactory;
    }

    @Override
    public <T> SelectBuilder<T> select(Class<T> type) {
        SelectContext<T> context = new SelectContext<>(type);
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
        UpdateContext<T> context = new UpdateContext<>(type);
        return new DefaultUpdateBuilder<>(context,
                updateProcessorFactoryFactory.getFactory(UpdateOperation.create));
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type);
        return new DefaultUpdateBuilder<>(context,
                updateProcessorFactoryFactory.getFactory(UpdateOperation.createOrUpdate));
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type);
        return new DefaultUpdateBuilder<>(context,
                updateProcessorFactoryFactory.getFactory(UpdateOperation.idempotentCreateOrUpdate));
    }

    /**
     * @since 1.7
     */
    @Override
    public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type);
        return new DefaultUpdateBuilder<>(context,
                updateProcessorFactoryFactory.getFactory(UpdateOperation.idempotentFullSync));
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> update(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type);
        return new DefaultUpdateBuilder<>(context,
                updateProcessorFactoryFactory.getFactory(UpdateOperation.update));
    }

    /**
     * @since 5.0
     */
    @Override
    public <T> UnrelateBuilder<T> unrelate(Class<T> type) {
        UnrelateContext<T> context = new UnrelateContext<>(type);
        return new DefaultUnrelateBuilder<>(context, unrelateProcessorFactory);
    }

    /**
     * @since 1.4
     */
    @Override
    public <T> DeleteBuilder<T> delete(Class<T> type) {
        DeleteContext<T> context = new DeleteContext<>(type);
        return new DefaultDeleteBuilder<>(context, deleteProcessorFactory);
    }

    @Override
    public <T> MetadataBuilder<T> metadata(Class<T> type) {
        MetadataContext<T> context = new MetadataContext<>(type);
        return new DefaultMetadataBuilder<>(context, metadataProcessorFactory);
    }
}
