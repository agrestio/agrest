package com.nhl.link.rest.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.LrPersistentRelationship;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.parser.converter.Normalizer;
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

	private ConstraintsBuilder<T> readConstraints;
	private ConstraintsBuilder<T> writeConstraints;

	private boolean includeData;
	protected boolean onDeleteUnrelate;

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
	public UpdateBuilder<T> readConstraints(ConstraintsBuilder<T> constraints) {
		this.readConstraints = constraints;
		return this;
	}

	@Override
	public UpdateBuilder<T> writeConstraints(ConstraintsBuilder<T> constraints) {
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
		if (onDeleteUnrelate && parent == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "onDeleteUnrelate option is set to true, while parent is not specified");
		}

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
			create(response);
			return withObjects(response, Status.CREATED);
		case idempotentFullSync:
			idempotentFullSync(response);
			return withObjects(response);
		case createOrUpdate:
			createOrUpdate(response);
			return withObjects(response);
		case idempotentCreateOrUpdate:
			idempotentCreateOrUpdate(response);
			return withObjects(response);
		case update:
			update(response);
			return withObjects(response);
		default:
			throw new UnsupportedOperationException("Unsupported operation: " + operation);
		}
	}

	/**
	 * @since 1.4
	 */
	protected abstract UpdateResponse<T> createResponse();

	protected ObjRelationship relationshipFromParent() {

		if (parent == null) {
			return null;
		}

		LrRelationship r = metadataService.getLrRelationship(parent);
		if (r instanceof LrPersistentRelationship) {
			return ((LrPersistentRelationship) r).getObjRelationship();
		}

		return null;
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

			LrEntity<T> entity = response.getEntity().getLrEntity();

			LrPersistentAttribute pk = (LrPersistentAttribute) entity.getSingleId();

			EntityUpdate u = response.getFirst();
			u.getOrCreateId().put(pk.getDbAttribute().getName(), Normalizer.normalize(id, pk.getJavaType()));
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
									+ response.getEntity().getLrEntity().getName());
				}

				String parentIdKey = last.getJoins().get(0).getTargetName();
				for (EntityUpdate u : response.getUpdates()) {
					u.getOrCreateId().put(parentIdKey, parent.getId());
				}
			}
		}
	}

	protected UpdateResponse<T> withObjects(UpdateResponse<T> response) {
		return withObjects(response, Status.OK);
	}

	@SuppressWarnings("unchecked")
	protected UpdateResponse<T> withObjects(UpdateResponse<T> response, Status status) {

		// response objects are attached to EntityUpdate instances ... if
		// 'includeData' is true create a list of unique updated objects in the
		// order corresponding to their initial appearance in the update.
		// We do not have to guarantee the order of objects in response (and
		// only Sencha seems to care - see #46), but there's not much overhead
		// involved, so we are doing it for all clients, not just Sencha

		if (includeData) {

			// if there are dupes, the list size will be smaller... sizing it
			// pessimistically
			List<T> objects = new ArrayList<>(response.getUpdates().size());

			// 'seen' is for a less common case of multiple updates per object
			// in a request
			Set<ObjectId> seen = new HashSet<>();

			for (EntityUpdate u : response.getUpdates()) {

				Persistent o = (Persistent) u.getMergedTo();
				if (o != null && seen.add(o.getObjectId())) {
					objects.add((T) o);
				}
			}

			response = (UpdateResponse<T>) response.withObjects(objects);
		}

		return (UpdateResponse<T>) response.withEncoder(encoderService.makeEncoder(response)).withStatus(status);
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

	@Override
	public UpdateBuilder<T> onDeleteUnrelate() {
		this.onDeleteUnrelate = true;
		return this;
	}

	protected abstract void create(UpdateResponse<T> response);

	protected abstract void createOrUpdate(UpdateResponse<T> response);

	protected abstract void idempotentCreateOrUpdate(UpdateResponse<T> response);

	protected abstract void idempotentFullSync(UpdateResponse<T> response);

	protected abstract void update(UpdateResponse<T> response);

}
