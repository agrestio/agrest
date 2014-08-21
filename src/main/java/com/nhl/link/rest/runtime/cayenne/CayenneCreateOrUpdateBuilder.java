package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.BaseCreateOrUpdateBuilder;
import com.nhl.link.rest.runtime.CreateOrUpdateOperation;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

class CayenneCreateOrUpdateBuilder<T> extends BaseCreateOrUpdateBuilder<T> {

	private ICayennePersister persister;

	public CayenneCreateOrUpdateBuilder(Class<T> type, CreateOrUpdateOperation op, ICayennePersister persister,
			IEncoderService encoderService, IRequestParser requestParser, IMetadataService metadataService,
			IConstraintsHandler constraintsHandler) {
		super(type, op, encoderService, requestParser, metadataService, constraintsHandler);

		this.persister = persister;
		this.metadataService = metadataService;
	}

	@Override
	protected CayenneUpdateResponse<T> createResponse() {
		return new CayenneUpdateResponse<>(type, persister.newContext());
	}

	@Override
	protected List<T> create(final UpdateResponse<T> response) {

		return process(response, new ObjectLocator<T>() {

			@Override
			public T locate(ResponseObjectMapper<T> mapper, EntityUpdate u) {
				return mapper.create(u);
			}
		});
	}

	@Override
	protected List<T> update(UpdateResponse<T> response) {
		return process(response, new ObjectLocator<T>() {

			@Override
			public T locate(ResponseObjectMapper<T> mapper, EntityUpdate u) {
				T o = mapper.find(u);
				if (o == null) {
					ObjEntity entity = metadataService.getObjEntity(type);
					throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
							+ entity.getName() + "'");
				}

				return o;
			}
		});
	}

	@Override
	protected List<T> createOrUpdate(final UpdateResponse<T> response) {

		return process(response, new ObjectLocator<T>() {

			@Override
			public T locate(ResponseObjectMapper<T> mapper, EntityUpdate u) {
				T o = mapper.find(u);
				return o != null ? o : mapper.create(u);
			}
		});
	}

	@Override
	protected List<T> idempotentCreateOrUpdate(final UpdateResponse<T> response) {

		return process(response, new ObjectLocator<T>() {

			@Override
			public T locate(ResponseObjectMapper<T> mapper, EntityUpdate u) {

				if (!mapper.isIdempotent(u)) {
					throw new LinkRestException(Status.BAD_REQUEST, "Request is not idempotent.");
				}

				T o = mapper.find(u);
				return o != null ? o : mapper.create(u);
			}
		});
	}

	private void mergeChanges(ObjEntity entity, EntityUpdate entityUpdate, DataObject object) {

		// attributes
		for (Entry<String, Object> e : entityUpdate.getValues().entrySet()) {
			object.writeProperty(e.getKey(), e.getValue());
		}

		// to-one relationships
		if (!entityUpdate.getRelatedIds().isEmpty()) {
			ObjectContext context = object.getObjectContext();
			for (Entry<String, Object> e : entityUpdate.getRelatedIds().entrySet()) {
				if (e.getValue() == null) {
					object.setToOneTarget(e.getKey(), null, true);
					continue;
				}
				ObjRelationship relationship = (ObjRelationship) entity.getRelationship(e.getKey());
				ClassDescriptor relatedDescriptor = context.getEntityResolver().getClassDescriptor(
						relationship.getTargetEntityName());
				DataObject related = (DataObject) Cayenne.objectForPK(context, relatedDescriptor.getObjectClass(),
						e.getValue());
				if (related == null) {
					throw new LinkRestException(Status.NOT_FOUND, "Related object '"
							+ relationship.getTargetEntityName() + "' with ID '" + e.getValue() + "' is not found");
				}
				object.setToOneTarget(e.getKey(), related, true);
			}
		}
	}

	protected ResponseObjectMapper<T> createObjectMapper(UpdateResponse<T> response) {
		ObjectMapper mapper = this.mapper != null ? this.mapper : CayenneByIdObjectMapper.mapper();
		return mapper.forResponse(response);
	}

	protected List<T> process(UpdateResponse<T> response, ObjectLocator<T> locator) {

		int len = response.getUpdates().size();
		if (len == 0) {
			return Collections.emptyList();
		}

		ObjEntity entity = response.getEntity().getCayenneEntity();

		ResponseObjectMapper<T> mapper = createObjectMapper(response);
		ObjectRelator<T> relator = createRelator(mapper);

		List<T> created = new ArrayList<>(len);

		for (EntityUpdate u : response.getUpdates()) {
			T o = locator.locate(mapper, u);
			mergeChanges(entity, u, (DataObject) o);
			relator.relate(o);
			created.add(o);
		}

		((CayenneUpdateResponse<T>) response).getUpdateContext().commitChanges();
		return created;
	}

	protected ObjectRelator<T> createRelator(ResponseObjectMapper<T> mapper) {

		if (parentType == null) {
			return new ObjectRelator<T>() {

				@Override
				public void relate(T object) {
					// do nothing..
				}
			};
		}

		final DataObject parent = (DataObject) mapper.findParent();
		if (parent == null) {
			ObjEntity entity = metadataService.getObjEntity(parentType);
			throw new LinkRestException(Status.NOT_FOUND, "No parent object for ID '" + parentId + "' and entity '"
					+ entity.getName() + "'");
		}

		// TODO: check that relationship target is the same as <T> ??
		if (metadataService.getObjRelationship(parentType, relationshipFromParent).isToMany()) {
			return new ObjectRelator<T>() {
				@Override
				public void relate(T object) {
					parent.addToManyTarget(relationshipFromParent, (DataObject) object, true);
				}
			};
		} else {
			return new ObjectRelator<T>() {
				@Override
				public void relate(T object) {
					parent.setToOneTarget(relationshipFromParent, (DataObject) object, true);
				}
			};
		}
	}

	interface ObjectLocator<T> {
		T locate(ResponseObjectMapper<T> mapper, EntityUpdate u);
	}

	interface ObjectRelator<T> {
		void relate(T object);
	}
}
