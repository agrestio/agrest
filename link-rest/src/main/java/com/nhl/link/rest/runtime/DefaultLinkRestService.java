package com.nhl.link.rest.runtime;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.EntityDelete;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.MetadataBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.runtime.listener.IListenerService;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;
import com.nhl.link.rest.runtime.processor.delete.DeleteProcessorFactory;
import com.nhl.link.rest.runtime.processor.meta.MetadataContext;
import com.nhl.link.rest.runtime.processor.meta.MetadataProcessorFactory;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.select.SelectProcessorFactory;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateProcessorFactory;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import com.nhl.link.rest.runtime.processor.update.UpdateProcessorFactoryFactory;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Property;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;

/**
 * A backend-agnostic abstract {@link ILinkRestService} that can serve to
 * implement more specific versions.
 */
public class DefaultLinkRestService implements ILinkRestService {

    private IListenerService listenerService;
    private SelectProcessorFactory selectProcessorFactory;
    private DeleteProcessorFactory deleteProcessorFactory;
    private UpdateProcessorFactoryFactory updateProcessorFactoryFactory;
    private MetadataProcessorFactory metadataProcessorFactory;
    private UnrelateProcessorFactory unrelateProcessorFactory;

    public DefaultLinkRestService(
            @Inject SelectProcessorFactory selectProcessorFactory,
            @Inject DeleteProcessorFactory deleteProcessorFactory,
            @Inject UpdateProcessorFactoryFactory updateProcessorFactoryFactory,
            @Inject MetadataProcessorFactory metadataProcessorFactory,
            @Inject UnrelateProcessorFactory unrelateProcessorFactory,
            @Inject IListenerService listenerService) {

        this.selectProcessorFactory = selectProcessorFactory;
        this.deleteProcessorFactory = deleteProcessorFactory;
        this.updateProcessorFactoryFactory = updateProcessorFactoryFactory;
        this.metadataProcessorFactory = metadataProcessorFactory;
        this.unrelateProcessorFactory = unrelateProcessorFactory;
        this.listenerService = listenerService;
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
        return new DefaultSelectBuilder<>(context, selectProcessorFactory, listenerService);
    }

    /**
     * @since 1.2
     */
    @Override
    public SimpleResponse unrelate(Class<?> type, Object sourceId, Property<?> relationship) {
        return unrelate(type, sourceId, relationship.getName());
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
    public SimpleResponse unrelate(Class<?> type, Object sourceId, Property<?> relationship, Object targetId) {
        return unrelate(type, sourceId, relationship.getName(), targetId);
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
                updateProcessorFactoryFactory.getFactory(UpdateOperation.create),
                listenerService);
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type);
        return new DefaultUpdateBuilder<>(context,
                updateProcessorFactoryFactory.getFactory(UpdateOperation.createOrUpdate),
                listenerService);
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type);
        return new DefaultUpdateBuilder<>(context,
                updateProcessorFactoryFactory.getFactory(UpdateOperation.idempotentCreateOrUpdate),
                listenerService);
    }

    /**
     * @since 1.7
     */
    @Override
    public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type);
        return new DefaultUpdateBuilder<>(context,
                updateProcessorFactoryFactory.getFactory(UpdateOperation.idempotentFullSync),
                listenerService);
    }

    /**
     * @since 1.3
     */
    @Override
    public <T> UpdateBuilder<T> update(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type);
        return new DefaultUpdateBuilder<>(context,
                updateProcessorFactoryFactory.getFactory(UpdateOperation.update),
                listenerService);
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
