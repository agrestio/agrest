package com.nhl.link.rest.runtime.cayenne;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.BaseUpdateBuilder;
import com.nhl.link.rest.runtime.UpdateOperation;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.Map;

class CayenneUpdateBuilder<T> extends BaseUpdateBuilder<T> {

	protected final ObjEntity entity;

	private ICayennePersister persister;

	CayenneUpdateBuilder(Class<T> type, UpdateOperation op, ICayennePersister persister,
			IEncoderService encoderService, IRequestParser requestParser, IMetadataService metadataService,
			IConstraintsHandler constraintsHandler) {
		super(type, op, encoderService, requestParser, metadataService, constraintsHandler);

		this.persister = persister;
		this.metadataService = metadataService;

		if ((this.entity = persister.entityResolver().getObjEntity(type)) == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "ObjEntity not found for class: " + type.getName());
		}
	}

	@Override
	protected CayenneUpdateResponse<T> createResponse() {
		CayenneUpdateResponse<T> response = new CayenneUpdateResponse<>(type, persister.newContext());
		response.parent(parent);
		return response;
	}

	@Override
	protected void fetchObjects(UpdateResponse<T> responseBuilder) {
		SelectQuery<T> query = new SelectQuery<>(type);

		if (responseBuilder.getEntity().getQualifier() != null) {
			query.andQualifier(responseBuilder.getEntity().getQualifier());
		}

		Collection<T> objects = responseBuilder.getObjects();
		IdExpressionBuilder builder = IdExpressionBuilder.forEntity(entity, objects.size());
		for (T o : objects) {
			builder.appendId(
					((Persistent)o).getObjectId()
			);
		}
		query.andQualifier(builder.buildExpression());

		Map<String, ResourceEntity<?>> childrenMap = responseBuilder.getEntity().getChildren();
		for (Map.Entry<String, ResourceEntity<?>> child : childrenMap.entrySet()) {
			if (!entity.getRelationship(child.getKey()).isToMany()) {
				query.addPrefetch(Util.createPrefetch(
						child.getValue(), PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS, child.getKey()
				));
			}
		}

		persister.sharedContext().select(query);
	}

	@Override
	protected void create(final UpdateResponse<T> response) {
		ObjectRelator<T> relator = createRelator(response);
		new CreateStrategy<T>((CayenneUpdateResponse<T>) response, relator).sync();
	}

	@Override
	protected void update(UpdateResponse<T> response) {
		ResourceReader reader = new ResourceReader();
		ObjectMapper<T> mapper = createObjectMapper(response);
		ObjectRelator<T> relator = createRelator(response);
		new UpdateStrategy<>((CayenneUpdateResponse<T>) response, relator, reader, mapper).sync();
	}

	@Override
	protected void createOrUpdate(final UpdateResponse<T> response) {
		ResourceReader reader = new ResourceReader();
		ObjectMapper<T> mapper = createObjectMapper(response);
		ObjectRelator<T> relator = createRelator(response);
		new CreateOrUpdateStrategy<>((CayenneUpdateResponse<T>) response, relator, reader, mapper, false).sync();
	}

	@Override
	protected void idempotentCreateOrUpdate(final UpdateResponse<T> response) {
		ResourceReader reader = new ResourceReader();
		ObjectMapper<T> mapper = createObjectMapper(response);
		ObjectRelator<T> relator = createRelator(response);
		new CreateOrUpdateStrategy<>((CayenneUpdateResponse<T>) response, relator, reader, mapper, true).sync();
	}

	@Override
	protected void idempotentFullSync(UpdateResponse<T> response) {
		ResourceReader reader = new ResourceReader();
		ObjectMapper<T> mapper = createObjectMapper(response);
		ObjectRelator<T> relator = createRelator(response);
		new FullSyncStrategy<>((CayenneUpdateResponse<T>) response, relator, reader, mapper, true).sync();
	}

	protected ObjectMapper<T> createObjectMapper(UpdateResponse<T> response) {
		ObjectMapperFactory mapper = this.mapper != null ? this.mapper : ByIdObjectMapperFactory.mapper();
		return mapper.forResponse(response);
	}

	protected ObjectRelator<T> createRelator(UpdateResponse<T> response) {
		final EntityParent<?> parent = response.getParent();

		if (parent == null) {
			return new ObjectRelator<T>() {

				@Override
				public void relate(T object) {
					// do nothing..
				}
			};
		}

		final DataObject parentObject = (DataObject) Util.findById(
				((CayenneUpdateResponse<T>) response).getUpdateContext(), parent.getType(), parent.getId());

		if (parentObject == null) {
			LrEntity<?> entity = metadataService.getLrEntity(parent.getType());
			throw new LinkRestException(Status.NOT_FOUND, "No parent object for ID '" + parent.getId()
					+ "' and entity '" + entity.getName() + "'");
		}

		// TODO: check that relationship target is the same as <T> ??
		if (metadataService.getLrRelationship(parent).isToMany()) {
			return new ObjectRelator<T>() {
				@Override
				public void relate(T object) {
					parentObject.addToManyTarget(parent.getRelationship(), (DataObject) object, true);
				}
			};
		} else {
			return new ObjectRelator<T>() {
				@Override
				public void relate(T object) {
					parentObject.setToOneTarget(parent.getRelationship(), (DataObject) object, true);
				}
			};
		}
	}

	interface ObjectRelator<T> {
		void relate(T object);
	}
}
