package com.nhl.link.rest.runtime;

import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.processor.IProcessorFactory;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * A backend-agnostic abstract {@link ILinkRestService} that can serve to
 * implement more specific versions.
 */
public class DefaultLinkRestService implements ILinkRestService {

	private IMetadataService metadataService;
	private Map<Class<?>, Map<String, Processor<?, ?>>> processors;

	public DefaultLinkRestService(@Inject IProcessorFactory processorFactory, @Inject IMetadataService metadataService) {
		this.processors = processorFactory.processors();
		this.metadataService = metadataService;
	}

	@Override
	public <T> DataResponse<T> selectById(Class<T> root, Object id) {
		return select(root).byId(id).select();
	}

	@Override
	public <T> DataResponse<T> selectById(Class<T> root, Object id, UriInfo uriInfo) {
		return select(root).uri(uriInfo).byId(id).select();
	}

	@Override
	public <T> DataResponse<T> select(SelectQuery<T> query, UriInfo uriInfo) {
		return select(query).uri(uriInfo).select();
	}

	@Override
	public <T> SelectBuilder<T> select(Class<T> type) {
		SelectContext<T> context = new SelectContext<>(type);
		return new DefaultSelectBuilder<>(context, processor(context));
	}

	@Override
	public <T> SelectBuilder<T> select(SelectQuery<T> query) {

		Class<T> type = metadataService.getLrEntity(query).getType();
		SelectContext<T> context = new SelectContext<>(type);
		context.setSelect(query);

		return new DefaultSelectBuilder<>(context, processor(context));
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

		Processor<UnrelateContext<Object>, Object> processor = processor(context);
		processor.execute(context);

		return context.getResponse();
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

		Processor<UnrelateContext<Object>, Object> processor = processor(context);
		processor.execute(context);

		return context.getResponse();
	}

	@Override
	public SimpleResponse delete(Class<?> root, Object id) {
		return delete(root).id(id).delete();
	}

	/**
	 * @since 1.3
	 */
	@Override
	public <T> UpdateBuilder<T> create(Class<T> type) {
		UpdateContext<T> context = new UpdateContext<>(type);
		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.create.name()));
	}

	/**
	 * @since 1.3
	 */
	@Override
	public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
		UpdateContext<T> context = new UpdateContext<>(type);
		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.createOrUpdate.name()));
	}

	/**
	 * @since 1.3
	 */
	@Override
	public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
		UpdateContext<T> context = new UpdateContext<>(type);
		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.idempotentCreateOrUpdate.name()));
	}

	/**
	 * @since 1.7
	 */
	@Override
	public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
		UpdateContext<T> context = new UpdateContext<>(type);
		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.idempotentFullSync.name()));
	}

	/**
	 * @since 1.3
	 */
	@Override
	public <T> UpdateBuilder<T> update(Class<T> type) {
		UpdateContext<T> context = new UpdateContext<>(type);
		return new DefaultUpdateBuilder<>(context, processor(context, UpdateOperation.update.name()));
	}

	/**
	 * @since 1.4
	 */
	@Override
	public <T> DeleteBuilder<T> delete(Class<T> type) {
		DeleteContext<T> context = new DeleteContext<>(type);
		return new DefaultDeleteBuilder<>(context, processor(context));
	}

	protected <C extends ProcessingContext<T>, T> Processor<C, T> processor(C context) {
		return processor(context, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <C extends ProcessingContext<T>, T> Processor<C, T> processor(C context, String operation) {

		Map<String, Processor<?, ?>> forContextType = processors.get(context.getClass());
		if (forContextType == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, String.format(
					"Processor is unsupported for context type %s", context.getClass().getName()));
		}

		Processor processor = forContextType.get(operation);
		if (processor == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, String.format(
					"Processor is unsupported for context type %s and operation %s", context.getClass().getName(),
					operation));
		}

		return processor;
	}
}
