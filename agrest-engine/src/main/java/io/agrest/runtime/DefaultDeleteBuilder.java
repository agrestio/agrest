package io.agrest.runtime;

import io.agrest.*;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @since 1.4
 */
public class DefaultDeleteBuilder<T> implements DeleteBuilder<T> {

	protected DeleteContext<T> context;
	protected DeleteProcessorFactory processorFactory;

	public DefaultDeleteBuilder(DeleteContext<T> context, DeleteProcessorFactory processorFactory) {
		this.context = context;
		this.processorFactory = processorFactory;
	}

	@Override
	public DeleteBuilder<T> id(Object id) {
		context.addId(id);
		return this;
	}

	@Override
	public DeleteBuilder<T> id(Map<String, Object> ids) {

		ids.forEach((name, value) -> {
			if (value == null) {
				throw new AgException(Response.Status.NOT_FOUND, "Part of compound ID is null: " + name);
			}
		});

		context.addCompoundId(ids);
		return this;
	}

	@Override
	public DeleteBuilder<T> id(AgObjectId id) {
		context.addId(id);
		return this;
	}

	@Override
	public DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent));
		return this;
	}

	@Override
	public DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent));
		return this;
	}

	@Override
	public SimpleResponse delete() {
		processorFactory.createProcessor().execute(context);
		return context.createSimpleResponse();
	}
}
