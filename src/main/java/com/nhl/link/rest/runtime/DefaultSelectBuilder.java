package com.nhl.link.rest.runtime;

import java.util.Collection;
import java.util.HashMap;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.property.PropertyBuilder;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * @since 1.16
 */
public class DefaultSelectBuilder<T> implements SelectBuilder<T> {

	private static final Logger logger = LoggerFactory.getLogger(DefaultSelectBuilder.class);

	protected SelectContext<T> context;
	protected Processor<SelectContext<?>> processor;

	public DefaultSelectBuilder(Class<T> type, Processor<SelectContext<?>> processor) {
		this.context = new SelectContext<>(type);
		this.processor = processor;
	}

	public SelectContext<T> getContext() {
		return context;
	}

	@Override
	public SelectBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent.getName()));
		return this;
	}

	@Override
	public SelectBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent));
		return this;
	}

	@Override
	public SelectBuilder<T> parent(EntityParent<?> parent) {
		context.setParent(parent);
		return this;
	}

	@Override
	public SelectBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentId, relationshipFromParent.getName());
	}

	@Override
	public SelectBuilder<T> constraints(ConstraintsBuilder<T> constraints) {
		context.setTreeConstraints(constraints);
		return this;
	}

	@Override
	public SelectBuilder<T> fetchLimit(int limit) {
		getOrCreateSizeConstraints().fetchLimit(limit);
		return this;
	}

	@Override
	public SelectBuilder<T> fetchOffset(int offset) {
		getOrCreateSizeConstraints().fetchOffset(offset);
		return this;
	}

	private SizeConstraints getOrCreateSizeConstraints() {
		if (context.getSizeConstraints() == null) {
			context.setSizeConstraints(new SizeConstraints());
		}

		return context.getSizeConstraints();
	}

	@Override
	public SelectBuilder<T> uri(UriInfo uriInfo) {
		this.context.setUriInfo(uriInfo);
		return this;
	}

	@Override
	public SelectBuilder<T> dataEncoder(Encoder encoder) {
		this.context.setEncoder(encoder);
		return this;
	}

	@Override
	public SelectBuilder<T> autocompleteOn(Property<?> autocompleteProperty) {
		context.setAutocompleteProperty(autocompleteProperty != null ? autocompleteProperty.getName() : null);
		return this;
	}

	@Override
	public SelectBuilder<T> property(String name) {
		return property(name, PropertyBuilder.property());
	}

	@Override
	public SelectBuilder<T> property(String name, EntityProperty clientProperty) {
		if (context.getExtraProperties() == null) {
			context.setExtraProperties(new HashMap<String, EntityProperty>());
		}

		EntityProperty oldProperty = context.getExtraProperties().put(name, clientProperty);
		if (oldProperty != null) {
			logger.info("Overriding existing custom property '" + name + "', ignoring...");
		}

		return this;
	}

	@Override
	public SelectBuilder<T> byId(Object id) {
		// TODO: return a special builder that will preserve 'byId' strategy on
		// select

		if (id == null) {
			throw new LinkRestException(Status.NOT_FOUND, "Null 'id'");
		}

		context.setId(id);
		return this;
	}

	@Override
	public DataResponse<T> select() {
		// 'byId' behaving as "selectOne" is really legacy behavior of 1.1...
		// should deprecate eventually
		context.setAtMostOneObject(context.isById());

		processor.execute(context);
		return context.getResponse();
	}

	@Override
	public DataResponse<T> selectOne() {
		context.setAtMostOneObject(true);
		processor.execute(context);
		return context.getResponse();
	}
}
