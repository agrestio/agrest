package com.nhl.link.rest.runtime;

import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.DeleteStage;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;
import com.nhl.link.rest.runtime.processor.delete.DeleteProcessorFactory;
import org.apache.cayenne.exp.Property;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * @since 1.4
 */
public class DefaultDeleteBuilder<T> implements DeleteBuilder<T> {

	// TODO: support custom stages, instead of using empty placeholder for stages
	private static final EnumMap<DeleteStage, Processor<DeleteContext<?>>> PLACEHOLDER  = new EnumMap<>(DeleteStage.class);

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
				throw new LinkRestException(Response.Status.NOT_FOUND, "Part of compound ID is null: " + name);
			}
		});

		context.addCompoundId(ids);
		return this;
	}

	@Override
	public DeleteBuilder<T> id(LrObjectId id) {
		context.addId(id);
		return this;
	}

	@Override
	public DeleteBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent.getName()));
		return this;
	}

	@Override
	public DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, Property<T> relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent.getName()));
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
	public DeleteBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentId, relationshipFromParent.getName());
	}

	@Override
	public DeleteBuilder<T> toManyParent(Class<?> parentType, Map<String, Object> parentIds, Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentIds, relationshipFromParent.getName());
	}

	@Override
	public SimpleResponse delete() {
		processorFactory.createProcessor(PLACEHOLDER).execute(context);
		return context.createSimpleResponse();
	}
}
