package com.nhl.link.rest.runtime;

import java.util.Collection;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;

/**
 * @since 1.4
 */
public class DefaultDeleteBuilder<T> implements DeleteBuilder<T> {

	protected DeleteContext<T> context;
	private Processor<DeleteContext<?>> processor;

	public DefaultDeleteBuilder(Class<T> type, Processor<DeleteContext<?>> processor) {
		this.context = new DeleteContext<>(type);
		this.processor = processor;
	}

	@Override
	public DeleteBuilder<T> id(Object id) {
		context.setId(id);
		return this;
	}

	@Override
	public DeleteBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent.getName()));
		return this;
	}

	@Override
	public DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent));
		return this;
	}

	@Override
	public DeleteBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentId, relationshipFromParent.getName());
	}

	@Override
	public SimpleResponse delete() {
		processor.execute(context);
		return context.getResponse();
	}
}
