package com.nhl.link.rest.runtime;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * @since 1.3
 */
public abstract class BaseUpdateBuilder<T> implements UpdateBuilder<T> {

	protected CreateOrUpdateOperation operation;
	protected Class<T> type;
	private UriInfo uriInfo;
	protected Object id;

	protected EntityParent<?> parent;

	private IRequestParser requestParser;
	private IEncoderService encoderService;
	protected IMetadataService metadataService;
	private IConstraintsHandler constraintsHandler;

	private TreeConstraints<T> readConstraints;
	private TreeConstraints<T> writeConstraints;

	protected ObjectMapper mapper;

	public BaseUpdateBuilder(Class<T> type, CreateOrUpdateOperation op, IEncoderService encoderService,
			IRequestParser requestParser, IMetadataService metadataService, IConstraintsHandler constraintsHandler) {
		this.type = type;
		this.operation = op;
		this.requestParser = requestParser;
		this.encoderService = encoderService;
		this.metadataService = metadataService;
		this.constraintsHandler = constraintsHandler;
	}

	@Override
	public UpdateBuilder<T> with(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		return this;
	}

	@Override
	public UpdateBuilder<T> id(Object id) {
		this.id = id;
		return this;
	}

	@Override
	public UpdateBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
		this.parent = new EntityParent<>(parentType, parentId, relationshipFromParent.getName());
		return this;
	}

	@Override
	public UpdateBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		this.parent = new EntityParent<>(parentType, parentId, relationshipFromParent);
		return this;
	}

	@Override
	public UpdateBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentId, relationshipFromParent.getName());
	}

	@Override
	public UpdateBuilder<T> readConstraints(TreeConstraints<T> constraints) {
		this.readConstraints = constraints;
		return this;
	}

	@Override
	public UpdateBuilder<T> writeConstraints(TreeConstraints<T> constraints) {
		this.writeConstraints = constraints;
		return this;
	}

	/**
	 * @since 1.4
	 */
	@Override
	public UpdateBuilder<T> mapper(ObjectMapper mapper) {
		this.mapper = mapper;
		return this;
	}

	@Override
	public UpdateResponse<T> process(String entityData) {

		UpdateResponse<T> response = createResponse();

		// parse request
		requestParser.parseUpdate(response, uriInfo, entityData);

		// this handles single object update (or insert?)...
		processExplicitId(response);

		constraintsHandler.constrainUpdate(response, writeConstraints);

		// apply read constraints (TODO: should we only care about response
		// constraints after the commit?)
		constraintsHandler.constrainResponse(response, null, readConstraints);

		switch (operation) {
		case create:
			return withObjects(response, create(response), Status.CREATED);
		case createOrUpdate:
			return withObjects(response, createOrUpdate(response));
		case idempotentCreateOrUpdate:
			return withObjects(response, idempotentCreateOrUpdate(response));
		case update:
			return withObjects(response, update(response));
		default:
			throw new UnsupportedOperationException("Unsupported operation: " + operation);
		}
	}

	/**
	 * @since 1.4
	 */
	protected UpdateResponse<T> createResponse() {
		return new UpdateResponse<>(type).parent(parent);
	}

	protected ObjRelationship relationshipFromParent() {
		return parent != null ? metadataService.getObjRelationship(parent) : null;
	}

	protected void processExplicitId(UpdateResponse<T> response) {

		if (id != null) {
			processExplicitId(response, id, false);
		} else if (parent != null) {
			ObjRelationship fromParent = relationshipFromParent();
			if (fromParent != null && fromParent.isToDependentEntity()) {
				processExplicitId(response, parent.getId(), true);
			}
		}
	}

	private void processExplicitId(UpdateResponse<T> response, Object id, boolean propagated) {

		// id was specified explicitly ... this means a few things:
		// * we expect zero or one object
		// * if zero - create an empty update that will be attached to the ID.
		// * if more than one - throw...

		if (response.getUpdates().isEmpty()) {
			response.getUpdates().add(new EntityUpdate());
		}

		if (propagated) {
			response.getFirst().setPropagatedId(id);
		} else {
			response.getFirst().setId(id);
		}
	}

	protected UpdateResponse<T> withObjects(UpdateResponse<T> response, List<T> objects) {
		return withObjects(response, objects, Status.OK);
	}

	protected UpdateResponse<T> withObjects(UpdateResponse<T> response, List<T> objects, Status status) {
		return (UpdateResponse<T>) response.withObjects(objects).withEncoder(encoderService.makeEncoder(response))
				.withStatus(status);
	}

	protected abstract List<T> create(UpdateResponse<T> response);

	protected abstract List<T> createOrUpdate(UpdateResponse<T> response);

	protected abstract List<T> idempotentCreateOrUpdate(UpdateResponse<T> response);

	protected abstract List<T> update(UpdateResponse<T> response);

}
