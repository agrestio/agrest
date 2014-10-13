package com.nhl.link.rest.runtime;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * @since 1.7
 */
public abstract class BaseUpdateBuilder<T> implements UpdateBuilder<T> {

	protected UpdateOperation operation;
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

	private boolean includeData;

	protected ObjectMapperFactory mapper;

	public BaseUpdateBuilder(Class<T> type, UpdateOperation op, IEncoderService encoderService,
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
	public UpdateBuilder<T> mapper(ObjectMapperFactory mapper) {
		this.mapper = mapper;
		return this;
	}

	@Override
	public UpdateResponse<T> process(String entityData) {

		UpdateResponse<T> response = createResponse();

		if (includeData) {
			response.includeData();
		} else {
			response.excludeData();
		}

		// parse request
		requestParser.parseUpdate(response, uriInfo, entityData);

		processExplicitId(response);
		processParentId(response);

		constraintsHandler.constrainUpdate(response, writeConstraints);

		// apply read constraints (TODO: should we only care about response
		// constraints after the commit?)
		constraintsHandler.constrainResponse(response, null, readConstraints);

		switch (operation) {
		case create:
			return withObjects(response, create(response), Status.CREATED);
		case idempotentFullSync:
			return withObjects(response, idempotentFullSync(response));
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
	protected abstract UpdateResponse<T> createResponse();

	protected ObjRelationship relationshipFromParent() {
		return parent != null ? metadataService.getObjRelationship(parent) : null;
	}

	private void processExplicitId(UpdateResponse<T> response) {

		if (id != null) {

			// id was specified explicitly ... this means a few things:
			// * we expect zero or one object in the body
			// * if zero, create an empty update that will be attached to the
			// ID.
			// * if more than one - throw...

			if (response.getUpdates().isEmpty()) {
				response.getUpdates().add(new EntityUpdate());
			}

			// TODO: duplicate code from DataObjectProcessor - unify in a single
			// place
			ObjEntity entity = response.getEntity().getCayenneEntity();
			Collection<ObjAttribute> pks = entity.getPrimaryKeys();
			if (pks.size() != 1) {
				throw new IllegalStateException(String.format(
						"Compound ID should't be specified explicitly for entity '%s'", entity.getName()));
			}

			ObjAttribute pk = pks.iterator().next();

			EntityUpdate u = response.getFirst();
			u.getOrCreateId().put(pk.getDbAttributeName(), id);
			u.setExplicitId();
		}
	}

	private void processParentId(UpdateResponse<T> response) {

		if (parent != null && parent.getId() != null) {
			ObjRelationship fromParent = relationshipFromParent();
			if (fromParent != null && fromParent.isToDependentEntity()) {
				List<DbRelationship> dbRelationships = fromParent.getDbRelationships();

				DbRelationship last = dbRelationships.get(dbRelationships.size() - 1);

				if (last.getJoins().size() != 1) {
					throw new LinkRestException(Status.BAD_REQUEST,
							"Multi-join relationship propagation is not supported yet: "
									+ response.getEntity().getCayenneEntity().getName());
				}

				String parentIdKey = last.getJoins().get(0).getTargetName();
				for (EntityUpdate u : response.getUpdates()) {
					u.getOrCreateId().put(parentIdKey, parent.getId());
				}
			}
		}
	}

	protected UpdateResponse<T> withObjects(UpdateResponse<T> response, List<T> objects) {
		return withObjects(response, objects, Status.OK);
	}

	protected UpdateResponse<T> withObjects(UpdateResponse<T> response, List<T> objects, Status status) {
		return (UpdateResponse<T>) response.withObjects(objects).withEncoder(encoderService.makeEncoder(response))
				.withStatus(status);
	}

	@Override
	public UpdateBuilder<T> excludeData() {
		this.includeData = false;
		return this;
	}

	@Override
	public UpdateBuilder<T> includeData() {
		this.includeData = true;
		return this;
	}

	protected abstract List<T> create(UpdateResponse<T> response);

	protected abstract List<T> createOrUpdate(UpdateResponse<T> response);

	protected abstract List<T> idempotentCreateOrUpdate(UpdateResponse<T> response);

	protected abstract List<T> idempotentFullSync(UpdateResponse<T> response);

	protected abstract List<T> update(UpdateResponse<T> response);

}
