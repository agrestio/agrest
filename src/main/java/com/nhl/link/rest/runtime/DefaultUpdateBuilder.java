package com.nhl.link.rest.runtime;

import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.processor.ChainProcessor;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.listener.ListenersBuilder;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.7
 */
public class DefaultUpdateBuilder<T> implements UpdateBuilder<T> {

	private UpdateContext<T> context;
	private ProcessingStage<UpdateContext<T>, T> updateChain;
	private ListenersBuilder listenersBuilder;

	public DefaultUpdateBuilder(UpdateContext<T> context, ProcessingStage<UpdateContext<T>, T> updateChain,
			ListenersBuilder listenersBuilder) {
		this.context = context;
		this.updateChain = updateChain;
		this.listenersBuilder = listenersBuilder;
	}

	@Override
	public UpdateBuilder<T> uri(UriInfo uriInfo) {
		context.setUriInfo(uriInfo);
		return this;
	}

	@Override
	public UpdateBuilder<T> id(Object id) {
		context.setId(id);
		return this;
	}

	@Override
	public UpdateBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent.getName()));
		return this;
	}

	@Override
	public UpdateBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent));
		return this;
	}

	@Override
	public UpdateBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentId, relationshipFromParent.getName());
	}

	@Override
	public UpdateBuilder<T> readConstraints(ConstraintsBuilder<T> constraints) {
		context.setReadConstraints(constraints);
		return this;
	}

	@Override
	public UpdateBuilder<T> writeConstraints(ConstraintsBuilder<T> constraints) {
		context.setWriteConstraints(constraints);
		return this;
	}

	/**
	 * @since 1.4
	 */
	@Override
	public UpdateBuilder<T> mapper(ObjectMapperFactory mapper) {
		context.setMapper(mapper);
		return this;
	}

	@Override
	public UpdateBuilder<T> excludeData() {
		context.setIncludingDataInResponse(false);
		return this;
	}

	/**
	 * @since 1.19
	 */
	@Override
	public UpdateBuilder<T> listener(Object listener) {
		listenersBuilder.addListener(listener);
		return this;
	}

	@Override
	public UpdateBuilder<T> includeData() {
		context.setIncludingDataInResponse(true);
		return this;
	}

	@Override
	public UpdateResponse<T> process(String entityData) {
		context.setEntityData(entityData);
		context.setListeners(listenersBuilder.getListeners());

		ChainProcessor.execute(updateChain, context);
		return context.getResponse();
	}

}
