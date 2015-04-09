package com.nhl.link.rest.runtime.cayenne;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.DefaultDeleteBuilder;
import com.nhl.link.rest.runtime.DefaultSelectBuilder;
import com.nhl.link.rest.runtime.DefaultUpdateBuilder;
import com.nhl.link.rest.runtime.dao.EntityDao;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

public class CayenneDao<T> implements EntityDao<T> {

	private Class<T> type;

	private Processor<SelectContext<?>> selectProcessor;
	private Map<UpdateOperation, Processor<UpdateContext<?>>> updateProcessors;
	private Processor<DeleteContext<?>> deleteProcessor;
	private Processor<UnrelateContext<?>> unrelateProcessor;

	public CayenneDao(Class<T> type, Processor<SelectContext<?>> selectProcessor,
			Map<UpdateOperation, Processor<UpdateContext<?>>> updateProcessors,
			Processor<DeleteContext<?>> deletedProcessor, Processor<UnrelateContext<?>> unrelateProcessor) {
		this.type = type;
		this.selectProcessor = selectProcessor;
		this.updateProcessors = updateProcessors;
		this.deleteProcessor = deletedProcessor;
		this.unrelateProcessor = unrelateProcessor;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public SelectBuilder<T> forSelect() {
		return new DefaultSelectBuilder<>(type, selectProcessor);
	}

	@Override
	public SelectBuilder<T> forSelect(SelectQuery<T> query) {
		DefaultSelectBuilder<T> builder = new DefaultSelectBuilder<>(type, selectProcessor);
		builder.getContext().setSelect(query);
		return builder;
	}

	private Processor<UpdateContext<?>> getUpdateProcessor(UpdateOperation op) {
		Processor<UpdateContext<?>> p = updateProcessors.get(op);
		if (p == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Invalid operation: " + op);
		}

		return p;
	}

	@Override
	public UpdateBuilder<T> create() {
		return new DefaultUpdateBuilder<>(type, getUpdateProcessor(UpdateOperation.create));
	}

	@Override
	public UpdateBuilder<T> createOrUpdate() {
		return new DefaultUpdateBuilder<>(type, getUpdateProcessor(UpdateOperation.createOrUpdate));
	}

	@Override
	public UpdateBuilder<T> idempotentCreateOrUpdate() {
		return new DefaultUpdateBuilder<>(type, getUpdateProcessor(UpdateOperation.idempotentCreateOrUpdate));
	}

	@Override
	public UpdateBuilder<T> idempotentFullSync() {
		return new DefaultUpdateBuilder<>(type, getUpdateProcessor(UpdateOperation.idempotentFullSync));
	}

	@Override
	public UpdateBuilder<T> update() {
		return new DefaultUpdateBuilder<>(type, getUpdateProcessor(UpdateOperation.update));
	}

	@Override
	public DeleteBuilder<T> delete() {
		return new DefaultDeleteBuilder<>(type, deleteProcessor);
	}

	@Override
	public SimpleResponse unrelate(Object sourceId, String relationship) {

		UnrelateContext<?> context = new UnrelateContext<>(type);
		context.setParent(new EntityParent<>(getType(), sourceId, relationship));

		unrelateProcessor.execute(context);

		return context.getResponse();
	}

	@Override
	public SimpleResponse unrelate(Object sourceId, String relationship, Object targetId) {

		UnrelateContext<?> context = new UnrelateContext<>(type);
		context.setParent(new EntityParent<>(getType(), sourceId, relationship));
		context.setId(targetId);

		unrelateProcessor.execute(context);

		return context.getResponse();
	}
}
