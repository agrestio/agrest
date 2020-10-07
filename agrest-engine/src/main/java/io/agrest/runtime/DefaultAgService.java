package io.agrest.runtime;

import io.agrest.*;
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

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;

/**
 * A backend-agnostic abstract {@link IAgService} that can serve to
 * implement more specific versions.
 */
public class DefaultAgService implements IAgService {

    private SelectProcessorFactory selectProcessorFactory;
    private DeleteProcessorFactory deleteProcessorFactory;
    private UpdateProcessorFactoryFactory updateProcessorFactoryFactory;
    private MetadataProcessorFactory metadataProcessorFactory;
    private UnrelateProcessorFactory unrelateProcessorFactory;

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
    public <T> DataResponse<T> selectById(Class<T> root, Object id) {
        return select(root).byId(id).get();
    }

    @Override
    public <T> DataResponse<T> selectById(Class<T> root, Object id, UriInfo uriInfo) {
        return select(root).uri(uriInfo).byId(id).get();
    }

    @Override
    public <T> SelectBuilder<T> select(Class<T> type) {
        SelectContext<T> context = new SelectContext<>(type);
        return toSelectBuilder(context);
    }

    private <T> SelectBuilder<T> toSelectBuilder(SelectContext<T> context) {
        return new DefaultSelectBuilder<>(context, selectProcessorFactory);
    }

    /**
     * @since 1.2
     */
    @Override
    public <T> SimpleResponse unrelate(Class<T> type, Object sourceId, String relationship) {

        // TODO: should context 'type' be the target type, not "parent" type?

        UnrelateContext<T> context = new UnrelateContext<>(type, new EntityParent<>(type, sourceId, relationship));
        unrelateProcessorFactory.createProcessor().execute(context);
        return context.createSimpleResponse();
    }

    /**
     * @since 1.2
     */
    @Override
    public <T> SimpleResponse unrelate(Class<T> type, Object sourceId, String relationship, Object targetId) {

        // TODO: should context 'type' be the target type, not "parent" type?

        UnrelateContext<T> context = new UnrelateContext<>(
                type,
                new EntityParent<>(type, sourceId, relationship),
                targetId);

        unrelateProcessorFactory.createProcessor().execute(context);
        return context.createSimpleResponse();
    }

    @Override
    public SimpleResponse delete(Class<?> root, Object id) {
        return delete(root).id(id).delete();
    }

    @Override
    public SimpleResponse delete(Class<?> root, Map<String, Object> ids) {
        return delete(root).id(ids).delete();
    }

    @Override
    public <T> SimpleResponse delete(Class<T> root, Collection<EntityDelete<T>> deleted) {
        DeleteBuilder<T> builder = delete(root);
        deleted.forEach(entityDelete -> builder.id(entityDelete.getId()));
        return builder.delete();
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
