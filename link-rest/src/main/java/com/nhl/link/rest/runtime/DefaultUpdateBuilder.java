package com.nhl.link.rest.runtime;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.processor.ChainProcessor;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.cayenne.ByKeyObjectMapperFactory;
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
	public UpdateBuilder<T> id(Map<String, Object> ids) {

		for (Object id : ids.entrySet()) {
			if (id == null) {
				throw new LinkRestException(Response.Status.NOT_FOUND, "Part of compound ID is null");
			}
		}

		context.setCompoundId(ids);
		return this;
	}

	@Override
	public UpdateBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent.getName()));
		return this;
	}

	@Override
	public UpdateBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds,
			Property<T> relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent.getName()));
		return this;
	}

	@Override
	public UpdateBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent));
		return this;
	}

	@Override
	public UpdateBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent));
		return this;
	}

	@Override
	public UpdateBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentId, relationshipFromParent.getName());
	}

	@Override
	public UpdateBuilder<T> toManyParent(Class<?> parentType, Map<String, Object> parentIds,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentIds, relationshipFromParent.getName());
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

	/**
	 * @since 1.20
	 */
	@Override
	public UpdateBuilder<T> mapper(Property<?> property) {
		return mapper(ByKeyObjectMapperFactory.byKey(property));
	}

	/**
	 * @since 1.20
	 */
	@Override
	public UpdateBuilder<T> mapper(String propertyName) {
		return mapper(ByKeyObjectMapperFactory.byKey(propertyName));
	}

	/**
	 * @since 1.19
	 */
	@Override
	public UpdateBuilder<T> listener(Object listener) {
		listenersBuilder.addListener(listener);
		return this;
	}

	/**
	 * @since 1.19
	 */
	@Override
	public SimpleResponse sync(String entityData) {
		context.setEntityData(entityData);
		return doSync();
	}

	/**
	 * @since 1.20
	 */
	@Override
	public SimpleResponse sync(EntityUpdate<T> update) {
		return sync(Collections.singleton(update));
	}

	/**
	 * @since 1.20
	 */
	@Override
	public SimpleResponse sync(Collection<EntityUpdate<T>> updates) {
		context.setUpdates(updates);
		return doSync();
	}

	/**
	 * @since 1.19
	 */
	@Override
	public DataResponse<T> syncAndSelect(String entityData) {
		context.setEntityData(entityData);
		return doSyncAndSelect();
	}

	/**
	 * @since 1.20
	 */
	@Override
	public DataResponse<T> syncAndSelect(Collection<EntityUpdate<T>> updates) {
		context.setUpdates(updates);
		return doSyncAndSelect();
	}

	/**
	 * @since 1.20
	 */
	@Override
	public DataResponse<T> syncAndSelect(EntityUpdate<T> update) {
		return syncAndSelect(Collections.singleton(update));
	}

	private SimpleResponse doSync() {
		context.setIncludingDataInResponse(false);
		context.setListeners(listenersBuilder.getListeners());

		ChainProcessor.execute(updateChain, context);

		return context.createSimpleResponse();
	}

	private DataResponse<T> doSyncAndSelect() {
		context.setIncludingDataInResponse(true);
		context.setListeners(listenersBuilder.getListeners());

		ChainProcessor.execute(updateChain, context);

		return context.createDataResponse();
	}

}
