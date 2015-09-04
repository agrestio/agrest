package com.nhl.link.rest.runtime;

import java.util.Collection;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.ChainProcessor;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;

/**
 * @since 1.4
 */
public class DefaultDeleteBuilder<T> implements DeleteBuilder<T> {

	protected DeleteContext<T> context;
	private ProcessingStage<DeleteContext<T>, T> deleteChain;

	public DefaultDeleteBuilder(DeleteContext<T> context, ProcessingStage<DeleteContext<T>, T> deleteChain) {
		this.context = context;
		this.deleteChain = deleteChain;
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
		ChainProcessor.execute(deleteChain, context);
		return context.getResponse();
	}
}
