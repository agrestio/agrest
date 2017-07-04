package com.nhl.link.rest.runtime;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.EntityDelete;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.MetadataBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.processor.ChainProcessor;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.listener.EventGroup;
import com.nhl.link.rest.runtime.listener.IListenerService;
import com.nhl.link.rest.runtime.listener.ListenersBuilder;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.processor.IProcessorFactory;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;
import com.nhl.link.rest.runtime.processor.meta.MetadataContext;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import com.nhl.link.rest.runtime.processor.select.SelectProcessorFactory;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Property;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;

/**
 * A backend-agnostic abstract {@link ILinkRestService} that can serve to
 * implement more specific versions.
 */
public class DefaultLinkRestService implements ILinkRestService {

	private IListenerService listenerService;
	private IMetadataService metadataService;
	private Map<Class<?>, Map<String, ProcessingStage<?, ?>>> processors;
	private SelectProcessorFactory selectProcessorFactory;

	public DefaultLinkRestService(
			@Inject IProcessorFactory processorFactory,
			@Inject SelectProcessorFactory selectProcessorFactory,
			@Inject IMetadataService metadataService,
			@Inject IListenerService listenerService) {

		this.processors = processorFactory.processors();
		this.selectProcessorFactory = selectProcessorFactory;
		this.metadataService = metadataService;
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
		ListenersBuilder listenersBuilder = new ListenersBuilder(listenerService, context, EventGroup.select);
		return new DefaultSelectBuilder<>(context, selectProcessorFactory, listenersBuilder);
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
	public SimpleResponse unrelate(Class<?> type, Object sourceId, String relationship) {

		// TODO: should context 'type' be the target type, not "parent" type?

		@SuppressWarnings({ "rawtypes", "unchecked" })
		UnrelateContext<Object> context = new UnrelateContext(type);
		context.setParent(new EntityParent<>(type, sourceId, relationship));

		ProcessingStage<UnrelateContext<Object>, Object> chain = chain(context);
		ChainProcessor.execute(chain, context);

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
	public SimpleResponse unrelate(Class<?> type, Object sourceId, String relationship, Object targetId) {

		// TODO: should context 'type' be the target type, not "parent" type?

		@SuppressWarnings({ "rawtypes", "unchecked" })
		UnrelateContext<Object> context = new UnrelateContext(type);
		context.setParent(new EntityParent<>(type, sourceId, relationship));
		context.setId(targetId);

		ProcessingStage<UnrelateContext<Object>, Object> processor = chain(context);
		ChainProcessor.execute(processor, context);

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
		ListenersBuilder listenersBuilder = new ListenersBuilder(listenerService, context, EventGroup.update);

		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.create.name()), listenersBuilder);
	}

	/**
	 * @since 1.3
	 */
	@Override
	public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
		UpdateContext<T> context = new UpdateContext<>(type);
		ListenersBuilder listenersBuilder = new ListenersBuilder(listenerService, context, EventGroup.update);

		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.createOrUpdate.name()),
				listenersBuilder);
	}

	/**
	 * @since 1.3
	 */
	@Override
	public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
		UpdateContext<T> context = new UpdateContext<>(type);
		ListenersBuilder listenersBuilder = new ListenersBuilder(listenerService, context, EventGroup.update);

		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.idempotentCreateOrUpdate.name()),
				listenersBuilder);
	}

	/**
	 * @since 1.7
	 */
	@Override
	public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
		UpdateContext<T> context = new UpdateContext<>(type);
		ListenersBuilder listenersBuilder = new ListenersBuilder(listenerService, context, EventGroup.update);

		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.idempotentFullSync.name()),
				listenersBuilder);
	}

	/**
	 * @since 1.3
	 */
	@Override
	public <T> UpdateBuilder<T> update(Class<T> type) {
		UpdateContext<T> context = new UpdateContext<>(type);
		ListenersBuilder listenersBuilder = new ListenersBuilder(listenerService, context, EventGroup.update);

		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.update.name()), listenersBuilder);
	}

	/**
	 * @since 1.4
	 */
	@Override
	public <T> DeleteBuilder<T> delete(Class<T> type) {
		DeleteContext<T> context = new DeleteContext<>(type);
		return new DefaultDeleteBuilder<>(context, chain(context));
	}

	@Override
	public <T> MetadataBuilder<T> metadata(Class<T> type) {
		MetadataContext<T> context = new MetadataContext<>(type);
		context.setEntity(metadataService.getLrEntity(type));
		return new DefaultMetadataBuilder<>(context, chain(context));
	}

	protected <C extends ProcessingContext<T>, T> ProcessingStage<C, T> chain(C context) {
		return processor(context, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <C extends ProcessingContext<T>, T> ProcessingStage<C, T> processor(C context, String operation) {

		Map<String, ProcessingStage<?, ?>> forContextType = processors.get(context.getClass());
		if (forContextType == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					String.format("Processor is unsupported for context type %s", context.getClass().getName()));
		}

		ProcessingStage processor = forContextType.get(operation);
		if (processor == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					String.format("Processor is unsupported for context type %s and operation %s",
							context.getClass().getName(), operation));
		}

		return processor;
	}
}
